package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Component;
import cn.autolabor.module.networkhub.dependency.Hook;
import cn.autolabor.module.networkhub.remote.resources.ServerSockets;
import cn.autolabor.module.networkhub.remote.utilities.SimpleOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public final class ShortConnectionServer
        extends AbstractDependent<ShortConnectionServer> {

    private final Hook<ServerSockets> servers = buildHook(ServerSockets.class);
    private final HashMap<Byte, ShortConnectionListener> listeners = new HashMap<>();

    public ShortConnectionServer() {
        super(ShortConnectionServer.class);
    }

    @Override
    public boolean sync(Component dependency) {
        super.sync(dependency);
        if (dependency instanceof ShortConnectionListener) {
            ShortConnectionListener temp = (ShortConnectionListener) dependency;
            listeners.put(temp.getInterest(), temp);
        }
        return false;
    }

    public void invoke(int port) {
        try {
            Socket socket = servers.safeGet().get(port).accept();
            byte cmd = Utilities.ListenCommand(socket);

            SimpleOutputStream stream = new SimpleOutputStream(128);
            while (true) {
                int temp = socket.getInputStream().read();
                if (temp == 0)
                    break;
                stream.write(temp);
            }
            String name = new String(stream.core, 0, stream.available());
            ShortConnectionListener listener = listeners.get(cmd);
            if (listener != null)
                listener.process(name, socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
