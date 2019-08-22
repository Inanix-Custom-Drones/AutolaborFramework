package cn.autolabor.module.networkhub.remote.modules.group;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Hook;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastBroadcaster;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastListener;
import cn.autolabor.module.networkhub.remote.resources.Group;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * 组成员的管理器
 * 发现新成员时会自动调用函数
 */
public class GroupMonitor
        extends AbstractDependent<GroupMonitor>
        implements MulticastListener {

    private static final ArrayList<Byte> INTEREST_SET = new ArrayList<>();

    private final Hook<Group> group = buildHook(Group.class);
    private final Hook<MulticastBroadcaster> broadcaster = buildHook(MulticastBroadcaster.class);

    private final Consumer<String> detected;

    public GroupMonitor(Consumer<String> detected) {
        super(GroupMonitor.class);
        this.detected = detected;
    }

    /**
     * 要求组中的成员响应以证实存在性
     */
    public void yell() {
        broadcaster.tryApply(it -> it.broadcast(UdpCmd.YELL_ASK.id, new byte[0]));
    }

    @Override
    public Collection<Byte> getInterest() {
        return INTEREST_SET;
    }

    @Override
    public void process(String sender, UdpCmd cmd, byte[] payload) {
        if (sender.trim().equals(sender)) {
            Long last = group.safeGet().update(sender, System.currentTimeMillis());
            if (last == null && detected != null)
                detected.accept(sender);
        }

        if (cmd == UdpCmd.YELL_ASK)
            broadcaster.tryApply(it -> it.broadcast(UdpCmd.YELL_ACK.id, new byte[0]));
    }
}
