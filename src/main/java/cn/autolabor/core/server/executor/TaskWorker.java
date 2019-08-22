package cn.autolabor.core.server.executor;

import cn.autolabor.util.Strings;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class TaskWorker extends QueuedSynchronizer implements Runnable {

    private static final long serialVersionUID = 4495328579844110848L;

    final Thread thread;

    private volatile long completedTasks;
    private AbstractTask firstTask;
    private TaskExecutorServer taskExecutorServer;
    public boolean isStandard;


    public TaskWorker(AbstractTask firstTask, TaskExecutorServer taskExecutorServer, ThreadFactory threadFactory) {
        setState(-1);
        this.firstTask = firstTask;
        this.thread = threadFactory.newThread(this);
        if (this.firstTask == null) {
            isStandard = true;
            this.thread.setName(String.format("standard-thread-%s", Strings.getShortUUID()));
        } else {
            isStandard = false;
            this.thread.setName(String.format("preemptive-thread-%s", Strings.getShortUUID()));
        }
        this.taskExecutorServer = taskExecutorServer;
    }

    public TaskWorker(AbstractTask firstTask, TaskExecutorServer taskExecutorServer) {
        this(firstTask, taskExecutorServer, Executors.defaultThreadFactory());
    }

    @Override
    public void run() {
        taskExecutorServer.runWorker(this);
    }

    public void interruptIfStarted() {
        Thread t;
        if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
            try {
                t.interrupt();
            } catch (SecurityException ignore) {
            }
        }
    }

    public Thread getThread() {
        return thread;
    }

    public AbstractTask getTask() {
        return firstTask;
    }

    public void setTask(AbstractTask firstTask) {
        this.firstTask = firstTask;
    }

    public void addCompletedTasks() {
        this.completedTasks += 1;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }
}
