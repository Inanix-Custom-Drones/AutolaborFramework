package cn.autolabor.core.server.executor;

public interface TaskProxy extends Comparable<TaskProxy> {

    Integer getPriority();

    void before(AbstractTask task, String eventName, Object... params);

    void after(AbstractTask task, String eventName, Object result, Object... params);

    default boolean filter(AbstractTask task, String eventName) {
        return true;
    }

    @Override
    default int compareTo(TaskProxy o) {
        return -getPriority().compareTo(o.getPriority());
    }
}
