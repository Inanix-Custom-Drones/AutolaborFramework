package cn.autolabor.core.server.executor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorServerStatus {

    public static final int RUNNING = -1;
    public static final int STOP = 0;
    public static final int TIDYING = 1;
    public static final int TERMINATED = 2;

    private final AtomicInteger status = new AtomicInteger(RUNNING);
    private final AtomicInteger standardWorkCount = new AtomicInteger(0);
    private final AtomicInteger preemptiveWorkCount = new AtomicInteger(0);

    private final ReentrantLock lock = new ReentrantLock();

    private int maxStandardWorkCount;
    private int maxPreemptiveWorkCount;

    public ExecutorServerStatus(int maxStandardWorkCount, int maxPreemptiveWorkCount) {
        this.maxStandardWorkCount = maxStandardWorkCount;
        this.maxPreemptiveWorkCount = maxPreemptiveWorkCount;
    }

    public boolean isRunning() {
        return status.get() < STOP;
    }

    public int getStatus() {
        return status.get();
    }

    public int getWorkCount(boolean standard) {
        return standard ? standardWorkCount.get() : preemptiveWorkCount.get();
    }

    public boolean checkWorkerCount(boolean standard) {
        return standard ? (standardWorkCount.get() < maxStandardWorkCount) : (preemptiveWorkCount.get() <
                maxPreemptiveWorkCount);
    }

    public boolean increaseWorkerCount(boolean standard) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (isRunning() && checkWorkerCount(standard)) {
                if (standard) {
                    standardWorkCount.addAndGet(1);
                } else {
                    preemptiveWorkCount.addAndGet(1);
                }
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean decreaseWorkerCount(boolean standard) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (isRunning() && getWorkCount(standard) > 0) {
                if (standard) {
                    standardWorkCount.decrementAndGet();
                } else {
                    preemptiveWorkCount.decrementAndGet();
                }
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public ServerStatusCopy snap() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return new ServerStatusCopy(status.get(), standardWorkCount.get(), preemptiveWorkCount.get());
        } finally {
            lock.unlock();
        }
    }

    public boolean setStop() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return status.compareAndSet(RUNNING, STOP);
        } finally {
            lock.unlock();
        }
    }

    public boolean setTidying() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return standardWorkCount.get() == 0 && preemptiveWorkCount.get() == 0 && status.compareAndSet(STOP,
                    TIDYING);
        } finally {
            lock.unlock();
        }
    }

    public boolean setTerminated() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return status.compareAndSet(TIDYING, TERMINATED);
        } finally {
            lock.unlock();
        }
    }

    public int getMaxStandardWorkCount() {
        return maxStandardWorkCount;
    }

    public int getMaxPreemptiveWorkCount() {
        return maxPreemptiveWorkCount;
    }

    public class ServerStatusCopy {
        private int status;
        private int standardWorkCount;
        private int preemptiveWorkCount;

        public ServerStatusCopy(int status, int standardWorkCount, int preemptiveWorkCount) {
            this.status = status;
            this.standardWorkCount = standardWorkCount;
            this.preemptiveWorkCount = preemptiveWorkCount;
        }

        public int getStatus() {
            return status;
        }

        public int getStandardWorkCount() {
            return standardWorkCount;
        }

        public int getPreemptiveWorkCount() {
            return preemptiveWorkCount;
        }
    }

}
