package cn.autolabor.module.networkhub.remote.resources;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public final class Addresses extends AbstractComponent<Addresses> {
    private static final Inet4Address EmptyAddress;

    static {
        Inet4Address address;
        try {
            address = (Inet4Address) Inet4Address.getByAddress(new byte[4]);
        } catch (UnknownHostException e) {
            address = null;
        }
        EmptyAddress = address;
    }

    private final ConcurrentHashMap<String, InetSocketAddress> core = new ConcurrentHashMap<>();

    public Addresses() {
        super(Addresses.class);
    }

    public void set(String name, Inet4Address address) {
        core.compute(name, (x, last) ->
                new InetSocketAddress(address, last == null ? 0 : last.getPort()));
    }

    public void set(String name, int port) {
        core.compute(name, (x, last) ->
                new InetSocketAddress(
                        last == null
                                ? EmptyAddress
                                : last.getAddress(),
                        port
                )
        );
    }

    public void set(String name, InetSocketAddress address) {
        core.put(name, address);
    }

    public InetSocketAddress get(String name) {
        InetSocketAddress temp = core.get(name);
        return temp == null || temp.getPort() == 0 ? null : temp;
    }

    public void remove(String name) {
        core.remove(name);
    }
}
