package cn.autolabor.module.networkhub.remote.modules.multicast;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 通用组播协议，接收通用组播 [COMMON] = 127
 */
public final class CommonUdpServer
        extends AbstractComponent<CommonUdpServer>
        implements MulticastListener {

    private static final List<Byte> INTEREST_SET = new ArrayList<>(1);

    static {
        INTEREST_SET.add(UdpCmd.COMMON.id);
    }

    private final BiConsumer<String, byte[]> received;

    public CommonUdpServer(BiConsumer<String, byte[]> received) {
        super(CommonUdpServer.class);
        this.received = received;
    }

    @Override
    public Collection<Byte> getInterest() {
        return INTEREST_SET;
    }

    @Override
    public void process(String sender, UdpCmd cmd, byte[] payload) {
        received.accept(sender, payload);
    }
}
