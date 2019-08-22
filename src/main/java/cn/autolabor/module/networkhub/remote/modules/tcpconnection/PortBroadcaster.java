package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Hook;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastBroadcaster;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastListener;
import cn.autolabor.module.networkhub.remote.resources.Name;
import cn.autolabor.module.networkhub.remote.resources.ServerSockets;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;

import java.util.Collection;
import java.util.HashSet;

/**
 * 端口同步机制 2
 * 这个模块用于 TCP 连接的接收者
 * 因此必须具备有效的 TCP 监听套接字和名字，并依赖组播收发
 */
public class PortBroadcaster
        extends AbstractDependent<PortBroadcaster>
        implements MulticastListener {

    private static final HashSet<Byte> INTEREST_SET = new HashSet<>();

    static {
        INTEREST_SET.add(UdpCmd.ADDRESS_ASK.id);
    }

    private final Hook<Name> name = buildHook(Name.class);
    private final Hook<MulticastBroadcaster> broadcaster = buildHook(MulticastBroadcaster.class);
    private final Hook<ServerSockets> servers = buildHook(ServerSockets.class);

    public PortBroadcaster() {
        super(PortBroadcaster.class);
    }

    @Override
    public Collection<Byte> getInterest() {
        return INTEREST_SET;
    }

    @Override
    public void process(String sender, UdpCmd cmd, byte[] payload) {
        if (!new String(payload).equals(name.safeGet().value))
            return;
        int port = servers.safeGet().defaultSocket.get().getLocalPort();
        broadcaster.safeGet().broadcast(
                UdpCmd.ADDRESS_ACK.id,
                new byte[]{(byte) (port >> 8), (byte) port}
        );
    }
}
