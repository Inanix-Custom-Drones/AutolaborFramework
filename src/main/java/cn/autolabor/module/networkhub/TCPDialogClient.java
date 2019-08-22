package cn.autolabor.module.networkhub;

import cn.autolabor.core.annotation.FilterTask;
import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.annotation.TaskParameter;
import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.module.communication.TCPRequest;
import cn.autolabor.module.communication.TCPRespStatusType;
import cn.autolabor.module.communication.TCPResponse;
import cn.autolabor.module.networkhub.remote.modules.group.GroupMonitor;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.PortMonitor;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.ShortConnectionClient;
import cn.autolabor.module.networkhub.remote.modules.tcpconnection.Utilities;
import cn.autolabor.module.networkhub.remote.resources.Addresses;
import cn.autolabor.module.networkhub.remote.resources.Group;
import cn.autolabor.module.networkhub.remote.resources.Name;
import cn.autolabor.module.networkhub.remote.resources.TcpCmd;
import cn.autolabor.util.autobuf.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TaskProperties(name = "TCPDialogClient")
public class TCPDialogClient extends AbstractTask {

    // Singleton ===============================

    private static ShortConnectionClient client;
    private static List<String> driverIds = new ArrayList<>();
    private static Addresses addresses;

    // =========================================

    static {
        ServerManager.me().register(new TCPDialogClient());
    }

    @TaskParameter(name = "deviceTimeout", value = "2000")
    private int deviceTimeout;
    private Group group;
    private GroupMonitor groupMonitor;
    private PortMonitor portMonitor;
    private boolean udpMulticastBroadcasterExist = false;
    private boolean udpMulticastReceiverExist = false;

    private TCPDialogClient(String... name) {
        super(name);
        RemoteHub.ME.setAndGet(new Name(ServerManager.getIdentification()));
        addresses = RemoteHub.ME.setAndGet(new Addresses());
        client = RemoteHub.ME.setAndGet(new ShortConnectionClient());
    }

    public static void startYell() {
    }

    /***
     *  request :  | taskName (end with 0x00) | eventName (end with 0x00) | params |
     *  response : | status (bit8) | result |
     */
    public static TCPResponse callOne(String deviceId, TCPRequest request) {
        Socket socket = client.connect(deviceId, TcpCmd.Dialog.id);
        if (socket != null) {
            try {
                ByteBuilder bb = new ByteBuilder();
                bb.putStringWithTag(request.getTaskName());
                bb.putStringWithTag(request.getEventName());
                if (request.hasParam()) {
                    Map<String, Object> params = request.getParams();
                    AutoBufEmbedded embedded = AutoBufBuilder.createEmbedded(null, "Params");
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        embedded.putRawData(entry.getKey(), entry.getValue());
                    }
                    bb.putBytes(AutoBufEncoder.toBytes(embedded));
                }
                Utilities.say(socket, bb.toBytes());
                ByteBuilder returnBB = new ByteBuilder(Utilities.listen(socket)).resetPosition();
                TCPResponse response = new TCPResponse();
                response.setStatus(TCPRespStatusType.getTypeFromCode(returnBB.readByte()));
                if (returnBB.getPosition() < returnBB.getLimit()) {
                    response.setResult(AutoBufDecoder.toObject(returnBB));
                }
                return response;
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            return new TCPResponse(TCPRespStatusType.NO_DEVICE, null);
        }
    }

    public static List<String> getDriverIds() {
        return driverIds;
    }

    @TaskFunction(name = "groupYell")
    public void groupYell() {
        groupMonitor.yell();
        ServerManager.me().run(this, 100L, "updateDrivers");
    }

    @TaskFunction(name = "updateDrivers")
    public void portAsk() {
        List<String> ids = group.get(deviceTimeout);
        driverIds = ids;
        for (String id : ids) {
            if (addresses.get(id) == null) {
                portMonitor.ask(id);
            }
        }
        ServerManager.me().run(this, (long) deviceTimeout, "groupYell");
    }

    @FilterTask
    @TaskFunction(name = "detectUDP")
    public void detectUDP(AbstractTask task) {
        if (!udpMulticastBroadcasterExist || !udpMulticastReceiverExist) {
            if (task.getClass().equals(UDPMulticastBroadcaster.class)) {
                udpMulticastBroadcasterExist = true;
            }
            if (task.getClass().equals(UDPMulticastReceiver.class)) {
                udpMulticastReceiverExist = true;
            }

            if (udpMulticastReceiverExist && udpMulticastBroadcasterExist) {
                group = RemoteHub.ME.setAndGet(new Group());
                groupMonitor = RemoteHub.ME.setAndGet(new GroupMonitor(null));
                portMonitor = RemoteHub.ME.setAndGet(new PortMonitor());
                ServerManager.me().removeRegisterTaskCallback(this, "detectUDP");
                ServerManager.me().run(this, "groupYell");
            }
        }
    }
}
