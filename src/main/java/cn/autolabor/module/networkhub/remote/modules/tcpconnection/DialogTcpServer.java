package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;
import cn.autolabor.module.networkhub.remote.resources.TcpCmd;

import java.net.Socket;
import java.util.function.BiFunction;

public class DialogTcpServer
        extends AbstractComponent<DialogTcpServer>
        implements ShortConnectionListener {

    private final BiFunction<String, byte[], byte[]> func;

    public DialogTcpServer(BiFunction<String, byte[], byte[]> func) {
        super(DialogTcpServer.class);
        this.func = func;
    }

    @Override
    public byte getInterest() {
        return TcpCmd.Dialog.id;
    }

    @Override
    public void process(String client, Socket socket) {
        Utilities.say(socket, func.apply(client, Utilities.listen(socket)));
    }
}
