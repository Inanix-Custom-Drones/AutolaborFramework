package cn.autolabor.module.networkhub.remote.resources;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

public final class Networks extends AbstractComponent<Networks> {

    private final HashMap<NetworkInterface, InterfaceAddress> core = new HashMap<>();

    public final Map<NetworkInterface, InterfaceAddress> view = Collections.unmodifiableMap(core);

    public Networks() {
        super(Networks.class);
        scan();
    }

    public void scan() {
        try {
            Enumeration<NetworkInterface> ptr = NetworkInterface.getNetworkInterfaces();
            Map<NetworkInterface, InterfaceAddress> networks = new HashMap<>();

            while (ptr.hasMoreElements()) {
                NetworkInterface network = ptr.nextElement();
                if (!network.isUp())
                    continue;
                if (network.isLoopback())
                    continue;
                if (!network.supportsMulticast())
                    continue;
                if (network.isVirtual())
                    continue;
                if (network.getName().toLowerCase().contains("virtual"))
                    continue;
                if (network.getDisplayName().toLowerCase().contains("virtual"))
                    continue;
                if (network.getName().toLowerCase().contains("docker"))
                    continue;
                if (network.getDisplayName().toLowerCase().contains("docker"))
                    continue;

                List<InterfaceAddress> list = network
                        .getInterfaceAddresses()
                        .stream()
                        .filter(it -> it.getAddress() instanceof Inet4Address)
                        .collect(Collectors.toList());

                if (list.size() != 1)
                    continue;
                InterfaceAddress address = list.get(0);
                int temp = address.getAddress().getAddress()[0] & 0xff;
                if (0 < temp && temp < 224 && temp != 127)
                    networks.put(network, address);
            }

            synchronized (core) {
                core.clear();
                core.putAll(networks);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public InterfaceAddress get(NetworkInterface networkInterface) {
        return core.get(networkInterface);
    }
}
