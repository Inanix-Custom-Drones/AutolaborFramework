package cn.autolabor.core.server.executor;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ScheduledPriorityQueue extends AbstractQueue<ScheduledPriorityItem> implements BlockingQueue<ScheduledPriorityItem> {

    private static final int INITIAL_CAPACITY = 16;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();
    private ScheduledPriorityItem[] nonTimeoutQueue = new ScheduledPriorityItem[INITIAL_CAPACITY];
    private ScheduledPriorityItem[] timeoutQueue = new ScheduledPriorityItem[INITIAL_CAPACITY];
    private int nonTimeoutSize;
    private int timeoutSize;
    private Thread leader;

    private void setIndex(ScheduledPriorityItem key, int k) {
        key.indexData.setIndex(k);
    }

    private void setItemType(ScheduledPriorityItem key, QueueIndexData.ItemType itemType) {
        key.indexData.setItemType(itemType);
    }

    private void setIndexData(ScheduledPriorityItem key, QueueIndexData.ItemType itemType, int index) {
        key.indexData.setItemType(itemType);
        key.indexData.setIndex(index);
    }

    private void siftUp(ScheduledPriorityItem key, int k) {
        if (!key.indexData.getItemType().equals(QueueIndexData.ItemType.NONE)) {
            ScheduledPriorityItem[] queue = key.indexData.getItemType().equals(QueueIndexData.ItemType.TIMEOUT) ? timeoutQueue : nonTimeoutQueue;
            while (k > 0) {
                int parent = (k - 1) >>> 1;
                ScheduledPriorityItem e = queue[parent];
                if (key.compareTo(e) >= 0)
                    break;
                queue[k] = e;
                setIndex(e, k);
                k = parent;
            }
            queue[k] = key;
            setIndex(key, k);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDown(ScheduledPriorityItem key, int k) {
        if (!key.indexData.getItemType().equals(QueueIndexData.ItemType.NONE)) {
            boolean isTimeout = key.indexData.getItemType().equals(QueueIndexData.ItemType.TIMEOUT);
            ScheduledPriorityItem[] queue = isTimeout ? timeoutQueue : nonTimeoutQueue;
            int size = isTimeout ? timeoutSize : nonTimeoutSize;
            int half = size >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                ScheduledPriorityItem c = queue[child];
                int right = child + 1;
                if (right < size && c.compareTo(queue[right]) > 0)
                    c = queue[child = right];
                if (key.compareTo(c) <= 0)
                    break;
                queue[k] = c;
                setIndex(c, k);
                k = child;
            }
            queue[k] = key;
            setIndex(key, k);
        }
    }

    private void grow(boolean isTimeout) {
        int oldCapacity, newCapacity;
        if (isTimeout) {
            oldCapacity = this.timeoutQueue.length;
            newCapacity = oldCapacity + (oldCapacity >> 1);
            this.timeoutQueue = Arrays.copyOf(this.timeoutQueue, newCapacity < 0 ? Integer.MAX_VALUE : newCapacity);
        } else {
            oldCapacity = this.nonTimeoutQueue.length;
            newCapacity = oldCapacity + (oldCapacity >> 1);
            this.nonTimeoutQueue = Arrays.copyOf(this.nonTimeoutQueue, newCapacity < 0 ? Integer.MAX_VALUE : newCapacity);
        }
    }

    private QueueIndexData indexOf(ScheduledPriorityItem x) {
        if (x != null) {
            switch (x.indexData.getItemType()) {
                case TIMEOUT:
                    if (x.indexData.getIndex() >= 0 && x.indexData.getIndex() < timeoutSize && timeoutQueue[x.indexData.getIndex()] == x) {
                        return x.indexData;
                    }
                    break;
                case NON_TIMEOUT:
                    if (x.indexData.getIndex() >= 0 && x.indexData.getIndex() < nonTimeoutSize && nonTimeoutQueue[x.indexData.getIndex()] == x) {
                        return x.indexData;
                    }
                    break;
                case NONE:
                    break;
            }
            setIndexData(x, QueueIndexData.ItemType.NONE, -1);
            for (int i = 0; i < nonTimeoutSize; i++) {
                if (x.equals(nonTimeoutQueue[i])) {
                    setIndexData(x, QueueIndexData.ItemType.NON_TIMEOUT, i);
                    return x.indexData;
                }
            }
            for (int i = 0; i < timeoutSize; i++) {
                if (x.equals(timeoutQueue[i])) {
                    setIndexData(x, QueueIndexData.ItemType.TIMEOUT, i);
                    return x.indexData;
                }
            }
            return new QueueIndexData(QueueIndexData.ItemType.NONE, -1);
        }
        return new QueueIndexData(QueueIndexData.ItemType.NONE, -1);
    }

    public void advance(ScheduledPriorityItem x) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            QueueIndexData indexData = indexOf(x);
            this.siftUp(x, indexData.getIndex());
            if (indexData.getItemType().equals(QueueIndexData.ItemType.NON_TIMEOUT)) {
                updateQueues();
            }
            available.signal();
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(ScheduledPriorityItem x) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return indexOf(x).getItemType() != QueueIndexData.ItemType.NONE;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(ScheduledPriorityItem x) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            QueueIndexData indexData = indexOf(x);
            int i = indexData.getIndex();
            if (i < 0 || indexData.getItemType().equals(QueueIndexData.ItemType.NONE)) {
                return false;
            }

            int s;
            ScheduledPriorityItem replacement;
            switch (indexData.getItemType()) {
                case TIMEOUT:
                    setIndexData(timeoutQueue[i], QueueIndexData.ItemType.NONE, -1);
                    s = --timeoutSize;
                    replacement = timeoutQueue[s];
                    timeoutQueue[s] = null;
                    if (s != i) {
                        siftDown(replacement, i);
                        if (timeoutQueue[i] == replacement) {
                            siftUp(replacement, i);
                        }
                    }
                    return true;
                case NON_TIMEOUT:
                    setIndexData(nonTimeoutQueue[i], QueueIndexData.ItemType.NONE, -1);
                    s = --nonTimeoutSize;
                    replacement = nonTimeoutQueue[s];
                    timeoutQueue[s] = null;
                    if (s != i) {
                        siftDown(replacement, i);
                        if (nonTimeoutQueue[i] == replacement) {
                            siftUp(replacement, i);
                        }
                    }
                    return true;
                default:
                    return false;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<ScheduledPriorityItem> iterator() {
        ScheduledPriorityItem[] array = new ScheduledPriorityItem[size()];
        System.arraycopy(nonTimeoutQueue, 0, array, 0, nonTimeoutSize);
        System.arraycopy(timeoutQueue, 0, array, nonTimeoutSize, timeoutSize);
        return new Itr(array);
    }

    @Override
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return nonTimeoutSize + timeoutSize;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(ScheduledPriorityItem abstractTask) throws InterruptedException {
        offer(abstractTask);
    }

    @Override
    public boolean offer(ScheduledPriorityItem abstractTask, long timeout, TimeUnit unit) throws InterruptedException {
        return offer(abstractTask);
    }

    private void updateQueues() {
        for (; ; ) {
            if (nonTimeoutSize <= 0) {
                return;
            } else {
                ScheduledPriorityItem nonTimeoutFirst = nonTimeoutQueue[0];
                if (nonTimeoutFirst != null && nonTimeoutFirst.isTimeout()) {
                    setItemType(nonTimeoutFirst, QueueIndexData.ItemType.TIMEOUT);
                    int j = timeoutSize++;
                    if (j >= timeoutQueue.length) {
                        grow(true);
                    }
                    siftUp(nonTimeoutFirst, j);
                    int i = --nonTimeoutSize;
                    if (i > 0) {
                        siftDown(nonTimeoutQueue[i], 0);
                    }
                } else {
                    return;
                }
            }
        }

    }

    private ScheduledPriorityItem finishPoll(ScheduledPriorityItem f) {
        int s = --timeoutSize;
        ScheduledPriorityItem x = timeoutQueue[s];
        timeoutQueue[s] = null;
        if (s != 0)
            siftDown(x, 0);
        setIndexData(f, QueueIndexData.ItemType.NONE, -1);
        return f;
    }

    @Override
    public ScheduledPriorityItem take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (; ; ) {
                updateQueues();
                if (nonTimeoutSize == 0 && timeoutSize == 0) {
                    available.await();
                } else if (timeoutSize > 0) {
                    return finishPoll(timeoutQueue[0]);
                } else {
                    long delay = nonTimeoutQueue[0].getDelay(TimeUnit.NANOSECONDS);
                    if (leader != null) {
                        available.await();
                    } else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            available.awaitNanos(delay);
                        } finally {
                            if (leader == thisThread) {
                                leader = null;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (leader == null && (nonTimeoutSize > 0 || timeoutSize > 0)) {
                available.signal();
            }
            lock.unlock();
        }
    }

    @Override
    public ScheduledPriorityItem poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (; ; ) {
                updateQueues();
                if (nonTimeoutSize == 0 && timeoutSize == 0) {
                    if (nanos <= 0L) {
                        return null;
                    } else {
                        nanos = available.awaitNanos(nanos);
                    }
                } else if (timeoutSize > 0) {
                    return finishPoll(timeoutQueue[0]);
                } else {
                    if (nanos <= 0L) {
                        return null;
                    }
                    long delay = nonTimeoutQueue[0].getDelay(TimeUnit.NANOSECONDS);
                    if (nanos < delay || leader != null) {
                        nanos = available.awaitNanos(nanos);
                    } else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            long timeLeft = available.awaitNanos(delay);
                            nanos -= delay - timeLeft;
                        } finally {
                            if (leader == thisThread) {
                                leader = null;
                            }
                        }
                    }
                }
            }
        } finally {
            if (leader == null && (nonTimeoutSize > 0 || timeoutSize > 0)) {
                available.signal();
            }
            lock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int drainTo(Collection<? super ScheduledPriorityItem> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            updateQueues();
            ScheduledPriorityItem first;
            int n = 0;
            while ((first = timeoutQueue[0]) != null) {
                c.add(first);
                finishPoll(first);
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super ScheduledPriorityItem> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            updateQueues();
            ScheduledPriorityItem first;
            int n = 0;
            while (n < maxElements && ((first = timeoutQueue[0]) != null)) {
                c.add(first);
                finishPoll(first);
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(ScheduledPriorityItem abstractTask) {
        if (abstractTask == null) {
            throw new NullPointerException();
        }
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i;
            if (abstractTask.isTimeout()) {
                setItemType(abstractTask, QueueIndexData.ItemType.TIMEOUT);
                i = timeoutSize;
                if (i >= timeoutQueue.length) {
                    grow(true);
                }
                timeoutSize = i + 1;
                if (i == 0) {
                    timeoutQueue[0] = abstractTask;
                    setIndex(abstractTask, 0);
                } else {
                    siftUp(abstractTask, i);
                }
                if (timeoutQueue[0] == abstractTask) {
                    leader = null;
                    available.signal();
                }
            } else {
                setItemType(abstractTask, QueueIndexData.ItemType.NON_TIMEOUT);
                i = nonTimeoutSize;
                if (i >= nonTimeoutQueue.length) {
                    grow(false);
                }
                nonTimeoutSize = i + 1;
                if (i == 0) {
                    nonTimeoutQueue[0] = abstractTask;
                } else {
                    siftUp(abstractTask, i);
                }
                if (nonTimeoutQueue[0] == abstractTask) {
                    leader = null;
                    available.signal();
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public ScheduledPriorityItem poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            updateQueues();
            return timeoutQueue[0];
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ScheduledPriorityItem peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            updateQueues();
            if (timeoutSize > 0) {
                return timeoutQueue[0];
            } else {
                return nonTimeoutQueue[0];
            }
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ScheduledPriorityItem k;
            for (int i = 0; i < timeoutSize; i++) {
                k = timeoutQueue[i];
                if (k != null) {
                    timeoutQueue[i] = null;
                    setIndexData(k, QueueIndexData.ItemType.NONE, -1);
                }
            }
            timeoutSize = 0;

            for (int i = 0; i < nonTimeoutSize; i++) {
                k = nonTimeoutQueue[i];
                if (k != null) {
                    nonTimeoutQueue[i] = null;
                    setIndexData(k, QueueIndexData.ItemType.NONE, -1);
                }
            }
            nonTimeoutSize = 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean add(ScheduledPriorityItem e) {
        return offer(e);
    }

    private class Itr implements Iterator<ScheduledPriorityItem> {
        final ScheduledPriorityItem[] array;
        int cursor;        // index of next element to return; initially 0
        int lastRet = -1;  // index of last element returned; -1 if no such

        Itr(ScheduledPriorityItem[] array) {
            this.array = array;
        }

        public boolean hasNext() {
            return cursor < array.length;
        }

        public ScheduledPriorityItem next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            return array[lastRet = cursor++];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            ScheduledPriorityQueue.this.remove(array[lastRet]);
            lastRet = -1;
        }
    }

}
