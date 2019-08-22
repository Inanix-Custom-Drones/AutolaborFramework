package cn.autolabor.module.networkhub.remote.modules.tcpconnection;

import cn.autolabor.module.networkhub.dependency.Component;

import java.net.Socket;

public interface ShortConnectionListener extends Component {
    byte getInterest();

    void process(String client, Socket socket);
}
