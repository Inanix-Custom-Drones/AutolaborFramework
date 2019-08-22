package cn.autolabor.module.networkhub.remote.resources;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;
import cn.autolabor.module.networkhub.dependency.LazyProperty;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerSockets extends AbstractComponent<ServerSockets> {
    public final LazyProperty<ServerSocket> defaultSocket;
    private final ConcurrentHashMap<Integer, ServerSocket> core = new ConcurrentHashMap<>();
    public final Map<Integer, ServerSocket> view = Collections.unmodifiableMap(core);

    public ServerSockets() {
        super(ServerSockets.class);
        defaultSocket = new LazyProperty<>(() -> buildWithoutException(0));
    }

    public ServerSockets(int port) {
        super(ServerSockets.class);
        defaultSocket = new LazyProperty<>(() -> buildWithoutException(port));
    }

    private static ServerSocket buildWithoutException(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ServerSocket get(Integer port) {
        return port == 0
                ? defaultSocket.get()
                : core.computeIfAbsent(port, it -> buildWithoutException(port));
    }
}
