package cn.autolabor.module.networkhub.remote.resources;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;
import cn.autolabor.module.networkhub.dependency.LazyProperty;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MulticastSockets extends AbstractComponent<MulticastSockets> implements Closeable {
    public final InetSocketAddress address;
    private final LazyProperty<MulticastSocket> defaultSocket;

    private final ConcurrentHashMap<NetworkInterface, MulticastSocket> core =
        new ConcurrentHashMap<>();

    public final Map<NetworkInterface, MulticastSocket> view =
        Collections.unmodifiableMap(core);

    public MulticastSockets(InetSocketAddress address) {
        super(MulticastSockets.class);
        this.address = address;
        defaultSocket = new LazyProperty<>(() -> multicastOn(this.address, null));
    }

    private static MulticastSocket multicastOn(
        InetSocketAddress group,
        NetworkInterface network
    ) {
        MulticastSocket result = null;
        try {
            result = new MulticastSocket(group.getPort());
            if (network != null)
                result.setNetworkInterface(network);
            result.joinGroup(group.getAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        defaultSocket.get().close();
        core.forEachValue(1, DatagramSocket::close);
    }

    public MulticastSocket get(NetworkInterface networkInterface) {
        return networkInterface == null
            ? defaultSocket.get()
            : core.computeIfAbsent
            (
                networkInterface,
                net -> multicastOn(address, net)
            );
    }
}
