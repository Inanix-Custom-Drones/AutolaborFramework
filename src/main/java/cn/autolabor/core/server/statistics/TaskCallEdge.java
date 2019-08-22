package cn.autolabor.core.server.statistics;

import cn.autolabor.util.collections.graph.Edge;

public class TaskCallEdge extends Edge {

    private long delayTime;
    private int count;

    public TaskCallEdge(long delayTime) {
        super(Long.toString(delayTime));
        this.delayTime = delayTime;
        this.count = 1;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        setName(Long.toString(delayTime));
        this.delayTime = delayTime;
    }

    public void merge(TaskCallEdge edge) {
        setDelayTime((delayTime * count + edge.getDelayTime() * edge.getCount()) / (count + edge.getCount()));
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    @Override
    public String toString() {
        return "TaskCallEdge{" + "delayTime=" + delayTime + '}';
    }
}
