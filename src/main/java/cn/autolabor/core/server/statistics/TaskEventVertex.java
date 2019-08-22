package cn.autolabor.core.server.statistics;

import cn.autolabor.util.collections.graph.Vertex;

public class TaskEventVertex extends Vertex {

    private String taskName;
    private String eventName;

    public TaskEventVertex(String taskName, String eventName) {
        super(String.format("%s.%s", taskName, eventName));
        this.taskName = taskName;
        this.eventName = eventName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        TaskEventVertex that = (TaskEventVertex) o;

        if (!taskName.equals(that.taskName))
            return false;
        return eventName.equals(that.eventName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + taskName.hashCode();
        result = 31 * result + eventName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TaskEventVertex{" + "taskName='" + taskName + '\'' + ", eventName='" + eventName + '\'' + '}';
    }
}
