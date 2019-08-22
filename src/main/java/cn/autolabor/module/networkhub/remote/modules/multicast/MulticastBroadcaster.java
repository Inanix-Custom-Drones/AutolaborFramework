package cn.autolabor.module.networkhub.remote.modules.multicast;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Hook;
import cn.autolabor.module.networkhub.remote.resources.MulticastSockets;
import cn.autolabor.module.networkhub.remote.resources.Name;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;
import cn.autolabor.module.networkhub.remote.utilities.SimpleOutputStream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * 组播发送器
 * 协议： | 名字 string 0x00 | cmd byte | payload byte[] |
 */
public class MulticastBroadcaster
        extends AbstractDependent<MulticastBroadcaster> {

    private final Hook<Name> name = buildHook(Name.class); // 可以匿名发送组播
    private final Hook<MulticastSockets> sockets = buildHook(MulticastSockets.class);
    private final Hook<PacketSlicer> slicer = buildHook(PacketSlicer.class);
    private final int size;

    public MulticastBroadcaster(int size) {
        super(MulticastBroadcaster.class);
        this.size = size;
    }

    public void broadcast(byte cmd, byte[] payload) {
        String me = name.tryLetOrDefault("", it -> it.value);

        if (me.length() == 0 && (cmd == UdpCmd.YELL_ACK.id || cmd == UdpCmd.ADDRESS_ACK.id))
            return;

        byte[] sender = me.getBytes();

        if (sender.length + 1 + 1 + payload.length <= size) {
            send(cmd, payload, sender, sender.length + 1 + 1 + payload.length);
        } else {
            boolean ok = slicer.tryLetOrDefault(null, it -> {
                it.broadcast(
                        cmd,
                        payload,
                        size - sender.length - 2,
                        sub -> send(UdpCmd.PACKET_SLICE.id, sub, sender, size));
                return new Object();
            }) != null;
            if (!ok)
                throw new RuntimeException("payload is too heavy!");
        }
    }

    private void send(byte cmd, byte[] payload, byte[] sender, int length) {
        SimpleOutputStream stream = new SimpleOutputStream(length);

        stream.write(sender);  //
        stream.write(0);       //
        stream.write(cmd);     //
        stream.write(payload); //

        DatagramPacket packet = new DatagramPacket(
                stream.core,
                stream.ptr,
                sockets.safeGet().address
        );

        try {
            for (MulticastSocket socket : sockets.safeGet().view.values())
                socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
