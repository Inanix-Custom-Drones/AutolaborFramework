package cn.autolabor.core.server.executor;

public class CallbackItem {

    private AbstractTask task;
    private String event;

    public CallbackItem(AbstractTask task, String event) {
        this.task = task;
        this.event = event;
    }

    public AbstractTask getTask() {
        return task;
    }

    public void setTask(AbstractTask task) {
        this.task = task;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CallbackItem eventItem = (CallbackItem) o;

        return task.equals(eventItem.task) && event.equals(eventItem.event);
    }

    @Override
    public int hashCode() {
        int result = task.hashCode();
        result = 31 * result + event.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CallbackItem{" +
                "task=" + task.getTaskName() +
                ", event='" + event + '\'' +
                '}';
    }
}
