package cn.autolabor.core.server.executor;

import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.ServerManager;

@TaskProperties(unique = false)
public class SimpleTask extends AbstractTask {

    SimpleTask(String parentName, String subName, int priority, boolean preemptive) {
        super(String.format("%s::%s", parentName, subName), String.format("priority=%d", priority), String.format("preemptive=%s", preemptive ? "true" : "false"), String.format("silentInit=true"));
        ServerManager.me().register(this);
    }

}
