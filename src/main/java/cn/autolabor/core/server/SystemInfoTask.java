package cn.autolabor.core.server;

import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.ExecutorServerStatus;
import cn.autolabor.core.server.statistics.GraphMessage;
import cn.autolabor.core.server.statistics.TaskRelationGraph;

import java.util.List;

@TaskProperties(name = "SystemInfoTask")
public class SystemInfoTask extends AbstractTask {

    public SystemInfoTask(String... name) {
        super(name);
    }

    // == executorServer ==

    @TaskFunction(name = "getWorkerCount")
    public int[] getWorkerCount() {
        ExecutorServerStatus.ServerStatusCopy s = ServerManager.me().executorServer.getExecutorServerStatus();
        return new int[]{s.getPreemptiveWorkCount() + s.getStandardWorkCount(), s.getStandardWorkCount(), s.getPreemptiveWorkCount()};
    }

    // == taskServer ==

    @TaskFunction(name = "getTaskCount")
    public int getTaskCount() {
        return ServerManager.me().taskServer.getTaskCount();
    }

    @TaskFunction(name = "getTaskNames")
    public List<String> getTaskNames() {
        return ServerManager.me().taskServer.getTaskNames();
    }

    @TaskFunction(name = "getEventNames")
    public List<String> getEventNames(String taskNames) {
        return ServerManager.me().taskServer.getTaskEvents(taskNames);
    }

    @TaskFunction(name = "getTaskRelationGraph")
    public GraphMessage getTaskRelationGraph() {
        TaskRelationGraph graph = new TaskRelationGraph();
        for (AbstractTask task : ServerManager.me().taskServer.getAllTasks()) {
            graph.merge(task.getRelationGraph());
        }
        return graph.toMessage();
    }

    // == messageServer ==

    @TaskFunction(name = "getTopicCount")
    public int getTopicCount() {
        return ServerManager.me().messageServer.getTopicCount();
    }


    public static void main(String[] args) {
        ServerManager.me().register(new SystemInfoTask());
        //        ServerManager.me().register(new Joy2Twist());
    }

}
