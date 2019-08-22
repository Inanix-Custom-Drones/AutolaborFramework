package cn.autolabor.module.networkhub.remote;

import cn.autolabor.module.networkhub.dependency.Component;
import cn.autolabor.module.networkhub.dependency.DynamicScope;
import cn.autolabor.module.networkhub.remote.modules.group.GroupMonitor;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastBroadcaster;
import cn.autolabor.module.networkhub.remote.modules.multicast.MulticastReceiver;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.PortBroadcaster;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.PortMonitor;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.ShortConnectionClient;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.ShortConnectionServer;
import cn.autolabor.module.networkhub.remote.resources.*;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class StandardRemoteHub {
    private final DynamicScope hub = new DynamicScope();

    private final Group group = new Group();                     // 成员存在性资源
    private final GroupMonitor monitor = new GroupMonitor(null); // 成员存在性管理

    private final Networks networks = new Networks(); // 本机网络端口资源
    private final MulticastSockets sockets;           // 组播套接字资源
    private final MulticastBroadcaster broadcaster = new MulticastBroadcaster(0x4000); // 组播广播装置
    private final MulticastReceiver receiver = new MulticastReceiver();          // 组播接收装置

    private final Addresses addresses = new Addresses();       // 远端地址资源
    private final ServerSockets servers = new ServerSockets(); // ServerSockets

    private final PortMonitor synchronizer1 = new PortMonitor();         // TCP 客户端，用于地址同步
    private final PortBroadcaster synchronizer2 = new PortBroadcaster(); // TCP 服务器，用于地址同步

    private final ShortConnectionClient client = new ShortConnectionClient(); // TCP 客户端，用于建立连接
    private final ShortConnectionServer server = new ShortConnectionServer(); // TCP 服务器，用于响应连接

    public StandardRemoteHub(
            String name,
            InetSocketAddress address,
            Collection<Component> additions
    ) {
        sockets = new MulticastSockets(address);

        hub.setup(new Name(name));

        hub.setup(group);
        hub.setup(monitor);

        hub.setup(networks);
        hub.setup(sockets);
        hub.setup(broadcaster);
        hub.setup(receiver);

        hub.setup(addresses);
        hub.setup(servers);
        hub.setup(synchronizer1);
        hub.setup(synchronizer2);

        hub.setup(client);
        hub.setup(server);

        if (additions != null)
            for (Component it : additions)
                hub.setup(it);
    }

    public Collection<Component> getModules() {
        return hub.components;
    }

    /**
     * 打开一个（随机的）网络接口
     *
     * @return 是否有一个网络接口已经打开了
     */
    public boolean OpenOneNetwork() {
        if (!sockets.view.isEmpty())
            return true;
        Optional<NetworkInterface> temp = networks.view.keySet().stream().findFirst();
        temp.ifPresent(sockets::get);
        return temp.isPresent();
    }

    /**
     * 打开全部网络接口
     *
     * @return 已经打开的网络接口数量
     */
    public int OpenAllNetworks() {
        networks.view.forEach((network, x) -> sockets.get(network));
        return sockets.view.size();
    }

    /**
     * 查看组成员
     *
     * @param timeout 存活时限
     * @return 时限内出现过的组成员名字
     */
    public List<String> membersBy(int timeout) {
        return group.get(timeout);
    }

    /**
     * 查看一个远端的端口号
     *
     * @param name 对方名字
     * @return 其地址和端口
     */
    public InetSocketAddress find(String name) {
        return addresses.get(name);
    }

    /**
     * 要求所有组成员自证存在性
     */
    public void yell() {
        monitor.yell();
    }

    /**
     * 查看一个远端的端口号
     *
     * @param name 对方名字
     */
    public void ask(String name) {
        synchronizer1.ask(name);
    }

    /**
     * 广播一包数据
     *
     * @param cmd     指令号
     * @param payload 负载
     */
    public void broadcast(byte cmd, byte[] payload) {
        broadcaster.broadcast(cmd, payload);
    }

    /**
     * 连接到一个远端
     *
     * @param name 对方名字
     * @param cmd  指令号
     * @return 套接字
     * <p>
     * 只包含连接机制，不进行重试，尚未得知对方地址或对方地址错误会触发一次询问，并返回空
     */
    public Socket connect(String name, byte cmd) {
        return client.connect(name, cmd);
    }

    /**
     * 启动UDP接收
     */
    public void invoke() {
        receiver.invoke();
    }

    /**
     * 启动TCP接收
     */
    public void accept() {
        server.invoke(0);
    }
}
