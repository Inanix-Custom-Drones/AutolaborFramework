package cn.autolabor.core.server.executor;

import cn.autolabor.util.Sugar;

import java.util.concurrent.TimeUnit;

public abstract class ScheduledPriorityItem implements PriorityItemInterface, ScheduledItemInterface, SequenceItemInterface, Comparable<ScheduledPriorityItem> {

    QueueIndexData indexData = new QueueIndexData(QueueIndexData.ItemType.NONE, -1);

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(getTime() - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public boolean isTimeout() {
        return getDelay(TimeUnit.NANOSECONDS) <= 0;
    }

    @Override
    public int compareTo(ScheduledPriorityItem other) {
        Sugar.checkNull(other);
        if (this.indexData.getItemType().equals(QueueIndexData.ItemType.NONE) || other.indexData.getItemType().equals(QueueIndexData.ItemType.NONE)) {
            return 0;
        } else if (!this.indexData.getItemType().equals(other.indexData.getItemType())) {
            return this.indexData.getItemType().equals(QueueIndexData.ItemType.NON_TIMEOUT) ? 1 : -1;
        } else if (this.indexData.getItemType().equals(QueueIndexData.ItemType.TIMEOUT) && (this.getPriority() != other.getPriority())) {
            return this.getPriority() < other.getPriority() ? 1 : -1;
        } else {
            long diff = getTime() - other.getTime();
            return diff < 0 ? -1 : (diff > 0 ? 1 : (getSequence() < other.getSequence() ? -1 : 1));
        }
    }
}
