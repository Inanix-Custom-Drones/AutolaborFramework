package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Hook;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastBroadcaster;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastListener;
import cn.autolabor.module.networkhub.remote.resources.Addresses;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;

import java.util.Collection;
import java.util.HashSet;

public final class PortMonitor
        extends AbstractDependent<PortMonitor>
        implements MulticastListener {

    private static final HashSet<Byte> INTEREST_SET = new HashSet<>();

    static {
        INTEREST_SET.add(UdpCmd.ADDRESS_ACK.id);
    }

    private final Hook<MulticastBroadcaster> broadcaster = buildHook(MulticastBroadcaster.class);
    private final Hook<Addresses> addresses = buildHook(Addresses.class);

    public PortMonitor() {
        super(PortMonitor.class);
    }

    public void ask(String name) {
        broadcaster.safeGet().broadcast(UdpCmd.ADDRESS_ASK.id, name.getBytes());
    }

    @Override
    public Collection<Byte> getInterest() {
        return INTEREST_SET;
    }

    @Override
    public void process(String sender, UdpCmd cmd, byte[] payload) {
        if (!sender.trim().equals(""))
            addresses.safeGet().set(sender, (payload[0] & 0xff) << 8 | payload[1] & 0xff);
    }
}
