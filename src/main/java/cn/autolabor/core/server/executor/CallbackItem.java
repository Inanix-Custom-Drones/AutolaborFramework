package cn.autolabor.core.server.executor;

import cn.autolabor.util.lambda.LambdaFunWithName;

import java.util.Objects;

public class CallbackItem {

    private AbstractTask task;
    private LambdaFunWithName event;

    public CallbackItem(AbstractTask task, LambdaFunWithName event) {
        this.task = task;
        this.event = event;
    }

    public AbstractTask getTask() {
        return task;
    }

    public void setTask(AbstractTask task) {
        this.task = task;
    }

    public LambdaFunWithName getEvent() {
        return event;
    }

    public void setEvent(LambdaFunWithName event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CallbackItem that = (CallbackItem) o;

        if (!Objects.equals(task, that.task))
            return false;
        return Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        int result = task != null ? task.hashCode() : 0;
        result = 31 * result + (event != null ? event.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CallbackItem{" + "task=" + task + ", event=" + event + '}';
    }
}
