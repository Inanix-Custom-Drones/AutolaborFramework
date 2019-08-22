package cn.autolabor.core.server.executor;

import cn.autolabor.util.lambda.LambdaFunWithName;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TaskExecutorServer {

    private final ScheduledPriorityQueue workQueue;
    private final HashSet<TaskWorker> workers = new HashSet<>();
    private final ReentrantLock mainLock = new ReentrantLock();

    private volatile ExecutorServerStatus executorServerStatus;

    private volatile AtomicLong completedTaskCount;

    public TaskExecutorServer(int standardPoolSize) {
        this(standardPoolSize, new ScheduledPriorityQueue());
    }

    public TaskExecutorServer(int standardPoolSize, ScheduledPriorityQueue scheduledPriorityQueue) {
        if (standardPoolSize < 0) {
            throw new IllegalArgumentException();
        }
        this.executorServerStatus = new ExecutorServerStatus(standardPoolSize, Integer.MAX_VALUE);
        this.workQueue = scheduledPriorityQueue;
        addWorker(null);
    }

    public void schedule(AbstractTask task, Long executeTime, LambdaFunWithName function, Object... params) {
        if (executorServerStatus.isRunning()) {
            task.addWaitingData(executeTime, function.getName(), function.getFun(), params); // 任务候选队列
            if (!task.register) {
                return;
            }
            checkOrder(task, executeTime);
            tryStart(task);
        } else {
            task.cancel(false);
        }
    }

    public void schedule(AbstractTask task, Long executeTime, String functionName, Object... params) {
        if (executorServerStatus.isRunning()) {
            task.addWaitingData(executeTime, functionName, params); // 任务候选队列
            if (!task.register) {
                return;
            }
            checkOrder(task, executeTime);
            tryStart(task);
        } else {
            task.cancel(false);
        }
    }

    public void checkOrder(AbstractTask task, Long executeTime) {
        if (task.isStandard()) { // 常规任务
            // 是否需要改变排序顺序
            if (!task.runLock.isLocked()) {
                task.waitdataLock.lock();
                try {
                    if (executeTime < task.getTime() && task.changeExecuteTime()) {
                        getQueue().advance(task);
                    }
                } finally {
                    task.waitdataLock.unlock();
                }
            }
        } else { // 抢占式任务
            if (!task.runLock.isLocked()) {
                task.waitdataLock.lock();
                try {
                    if (executeTime < task.getTime() && task.changeExecuteTime()) {
                        task.preemptiveWaitLock.lock();
                        try {
                            task.preemptiveWaitCondition.signal();
                        } finally {
                            task.preemptiveWaitLock.unlock();
                        }
                    }
                } finally {
                    task.waitdataLock.unlock();
                }
            }
        }
    }

    public void tryStart(AbstractTask task) {
        if (task.isStandard()) { // 常规任务
            // 是否需要触发执行
            if (task.handleFlag.compareAndSet(false, true)) {
                if (task.prepareData()) {
                    getQueue().add(task);
                    if (executorServerStatus.checkWorkerCount(true)) {
                        addWorker(null);
                    }
                } else {
                    task.handleFlag.set(false);
                }
            }
        } else { // 抢占式任务
            if (task.handleFlag.compareAndSet(false, true)) {
                if (task.prepareData()) {
                    if (executorServerStatus.checkWorkerCount(false)) {
                        addWorker(task);
                    }
                } else {
                    task.handleFlag.set(false);
                }
            }
        }
    }

    private void addWorker(AbstractTask firstTask) {
        boolean isStandard = ((firstTask == null) || firstTask.isStandard());
        if (executorServerStatus.increaseWorkerCount(isStandard)) {
            boolean workerStarted = false;
            boolean workerAdded = false;
            TaskWorker w = null;
            try {
                w = new TaskWorker(firstTask, this);
                final Thread t = w.thread;
                if (t != null) {
                    final ReentrantLock mainLock = this.mainLock;
                    mainLock.lock();
                    try {
                        if (executorServerStatus.isRunning()) {
                            if (t.isAlive()) {
                                throw new IllegalThreadStateException();
                            }
                            workerAdded = workers.add(w);
                        }
                    } finally {
                        mainLock.unlock();
                    }
                    if (workerAdded) {
                        t.start();
                        workerStarted = true;
                    }
                }
            } finally {
                if (!workerStarted) {
                    addWorkerFailed(w, isStandard);
                }
            }
        }
    }

    void runWorker(TaskWorker w) {
        boolean completedAbruptly = true;
        w.unlock();
        try {
            Thread wt = Thread.currentThread();
            AbstractTask task;
            if (w.isStandard) { // 标准任务
                while ((task = getTask()) != null) {
                    if (!executorServerStatus.isRunning() && !wt.isInterrupted()) {
                        wt.interrupt();
                    }
                    w.lock();
                    task.waitdataLock.lock();
                    try {
                        task.run();
                    } finally {
                        if (task.prepareData()) {
                            getQueue().add(task);
                        } else {
                            task.handleFlag.set(false);
                        }
                        task.waitdataLock.unlock();
                        w.unlock();
                    }
                }
            } else { // 抢占式
                task = w.getTask();
                try {
                    while (true) {
                        w.lock();
                        try {
                            task.preemptiveWaitLock.lock();
                            try {
                                long delay = task.getDelay(TimeUnit.MILLISECONDS);
                                while (delay > 0) {
                                    try {
                                        task.preemptiveWaitCondition.await(delay, TimeUnit.MILLISECONDS);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    delay = task.getDelay(TimeUnit.MILLISECONDS);
                                }
                            } finally {
                                task.preemptiveWaitLock.unlock();
                            }

                            task.waitdataLock.lock();
                            try {
                                task.run();
                                if (!task.prepareData()) {
                                    break;
                                }
                            } finally {
                                task.waitdataLock.unlock();
                            }
                        } finally {
                            w.unlock();
                        }
                    }
                } finally {
                    task.handleFlag.set(false);
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }

    }

    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            executorServerStatus.setStop();
            interruptWorkers();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    public boolean isRunning() {
        return this.executorServerStatus.isRunning();
    }

    private void processWorkerExit(TaskWorker w, boolean completedAbruptly) {
        if (completedAbruptly || !w.isStandard) // If abrupt, then workerCount wasn't adjusted
            executorServerStatus.decreaseWorkerCount(w.isStandard);

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            //            completedTaskCount.addAndGet(w.getCompletedTasks());
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }
        tryTerminate();

        if (executorServerStatus.isRunning() && w.isStandard) {
            if (!completedAbruptly) {
                int min = executorServerStatus.getMaxStandardWorkCount();
                if (min == 0 && !workQueue.isEmpty())
                    min = 1;
                if (executorServerStatus.getMaxStandardWorkCount() >= min)
                    return; // replacement not needed
            }
            addWorker(null);
        }
    }

    private void addWorkerFailed(TaskWorker w, boolean isStandard) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null) {
                workers.remove(w);
            }
            executorServerStatus.decreaseWorkerCount(isStandard);
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    private void tryTerminate() {
        for (; ; ) {
            ExecutorServerStatus.ServerStatusCopy snap = executorServerStatus.snap();
            if (snap.getStatus() != ExecutorServerStatus.STOP)
                return;

            if ((snap.getStandardWorkCount() + snap.getPreemptiveWorkCount()) != 0) {
                interruptIdleWorkers(true);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (executorServerStatus.setTidying()) {
                    executorServerStatus.setTerminated();
                    return;
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (TaskWorker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    private AbstractTask getTask() {
        for (; ; ) {
            if (!executorServerStatus.isRunning()) {
                executorServerStatus.decreaseWorkerCount(true);
                return null;
            }

            try {
                return (AbstractTask) workQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (TaskWorker w : workers) {
                w.interruptIfStarted();
            }
        } finally {
            mainLock.unlock();
        }
    }

    private ScheduledPriorityQueue getQueue() {
        return workQueue;
    }

    public ExecutorServerStatus.ServerStatusCopy getExecutorServerStatus() {
        return executorServerStatus.snap();
    }
}
