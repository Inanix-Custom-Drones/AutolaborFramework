package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Hook;
import cn.autolabor.module.networkhub.remote.resources.Addresses;
import cn.autolabor.module.networkhub.remote.resources.Name;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class ShortConnectionClient extends AbstractDependent<ShortConnectionClient> {

    private final Hook<Name> name = buildHook(Name.class);
    private final Hook<Addresses> addresses = buildHook(Addresses.class);
    private final Hook<PortMonitor> monitor = buildHook(PortMonitor.class);

    public ShortConnectionClient() {
        super(ShortConnectionClient.class);
    }

    public Socket connect(String server, byte cmd) {
        InetSocketAddress address = addresses.safeGet().get(server);
        if (address == null) {
            monitor.tryApply(it -> it.ask(server));
            return null;
        }

        Socket socket = new Socket();
        try {
            socket.connect(address);
            socket.getOutputStream().write(cmd);
            socket.getOutputStream().write(name.tryLetOrDefault("", it -> it.value).getBytes());
            socket.getOutputStream().write(0);
            return socket;
        } catch (IOException e) {
            addresses.safeGet().remove(server);
            monitor.tryApply(it -> it.ask(server));
            return null;
        }
    }
}
