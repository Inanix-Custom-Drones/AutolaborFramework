package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;
import cn.autolabor.module.networkhub.remote.resources.TcpCmd;

import java.net.Socket;
import java.util.function.BiConsumer;

public class CommonTcpServer
        extends AbstractComponent<CommonTcpServer>
        implements ShortConnectionListener {

    private final BiConsumer<String, Socket> func;

    public CommonTcpServer(BiConsumer<String, Socket> func) {
        super(CommonTcpServer.class);
        this.func = func;
    }

    @Override
    public byte getInterest() {
        return TcpCmd.COMMON.id;
    }

    @Override
    public void process(String client, Socket socket) {
        func.accept(client, socket);
    }
}
