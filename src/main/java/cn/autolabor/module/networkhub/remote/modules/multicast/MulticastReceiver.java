package cn.autolabor.module.networkhub.remote.modules.multicast;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Component;
import cn.autolabor.module.networkhub.dependency.Hook;
import cn.autolabor.module.networkhub.remote.resources.*;
import cn.autolabor.module.networkhub.remote.utilities.SimpleInputStream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组播接收器
 * 协议： | 名字 string 0x00 | cmd byte | payload byte[] |
 */
public final class MulticastReceiver
        extends AbstractDependent<MulticastReceiver> {

    private final int bufferSize;

    private final ThreadLocal<DatagramPacket> buffer = new ThreadLocal<>();
    private final Hook<Name> name = buildHook(Name.class);
    private final Hook<MulticastSockets> sockets = buildHook(MulticastSockets.class);
    private final HashSet<MulticastListener> listeners = new HashSet<>();

    private final Hook<Networks> networks = buildHook(Networks.class);
    private final Hook<Addresses> addresses = buildHook(Addresses.class);

    public MulticastReceiver() {
        super(MulticastReceiver.class);
        this.bufferSize = 65536;
    }

    public MulticastReceiver(int bufferSize) {
        super(MulticastReceiver.class);
        this.bufferSize = bufferSize;
    }

    private static int intOf(Inet4Address address) {
        int acc = 0;
        for (byte b : address.getAddress())
            acc = (acc << 8) | ((int) b & 0xff);
        return acc;
    }

    private static boolean match(InterfaceAddress a, Inet4Address b) {
        if (a.getAddress().equals(b))
            return true;
        int mask = Integer.MAX_VALUE << 32 - a.getNetworkPrefixLength();
        return (intOf(b) & mask) == (intOf((Inet4Address) a.getAddress()) & mask);
    }

    @Override
    public boolean sync(Component dependency) {
        super.sync(dependency);
        if (dependency instanceof MulticastListener)
            listeners.add((MulticastListener) dependency);
        return false;
    }

    public void invoke() {
        DatagramPacket bufferPacket = buffer.get();

        if (bufferPacket == null) {
            bufferPacket = new DatagramPacket(new byte[bufferSize], bufferSize);
            buffer.set(bufferPacket);
        }

        try {
            sockets
                    .safeGet()
                    .get(null)
                    .receive(bufferPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleInputStream stream = new SimpleInputStream(
                bufferPacket.getData(), 0, bufferPacket.getLength());

        String sender = stream.readEnd().trim(); // 收名字
        if (name.tryLetOrDefault("", it -> it.value).equals(sender))
            return;

        {
            Inet4Address address = (Inet4Address) bufferPacket.getAddress();

            List<Map.Entry<NetworkInterface, InterfaceAddress>> temp
                    = networks.tryLetOrDefault(null,
                    it -> it
                            .view
                            .entrySet()
                            .stream()
                            .filter(entry -> match(entry.getValue(), address))
                            .collect(Collectors.toList())
            );

            if (temp != null && temp.size() == 1)
                sockets.safeGet().get(temp.get(0).getKey());

            addresses.tryApply(it -> it.set(sender, address));
        }

        UdpCmd cmd = UdpCmd.memory.get((byte) stream.read());
        byte[] payload = stream.lookRest();

        //        System.out.println
        //            ("sender: " + sender + ", cmd: " + cmd + ", payload: byte[" + payload.length + "]");

        listeners
                .stream()
                .filter(it -> it.getInterest().isEmpty() || it.getInterest().contains(cmd.id))
                .forEach(it -> it.process(sender, cmd, payload));
    }
}
