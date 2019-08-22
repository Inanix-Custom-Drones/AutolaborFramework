package cn.autolabor.module.communication;

import cn.autolabor.core.annotation.FilterMessage;
import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.annotation.TaskParameter;
import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.core.server.message.MessageSource;
import cn.autolabor.core.server.message.MessageSourceType;
import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.Unsigned;
import cn.autolabor.util.autobuf.AutoBufAdapter;
import cn.autolabor.util.autobuf.ByteBuilder;
import cn.autolabor.util.collections.DoubleKeyMap;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/***
 * MESSAGE:
 *
 * | IDENTIFIES (String with end 0x00) | TOPIC (String with end 0x00) | SEQ (INT 4bytes [>0] no Fragment [<0] fragment) | --
 * | PAYLOAD_LENGTH (INT 4bytes) | PAYLOAD_FRAME_SIZE (SHORT 2bytes) | PAYLOAD_FRAME_INDEX (SHORT 2bytes) | --
 * | OFFSET (INT 4bytes) | PAYLOAD (n bytes) |
 *
 */


@TaskProperties(name = "UDPMulticastSupport")
public class UDPMulticastSupport extends AbstractTask {

    protected static Logger log = Logger.getLogger(UDPMulticastSupport.class.getName());

    private static AtomicInteger seq = new AtomicInteger(0);

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    @TaskParameter(name = "networkInterfaceName", value = "")
    private String networkInterfaceName;

    @TaskParameter(name = "ip", value = "238.88.08.07")
    private String ip;

    @TaskParameter(name = "port", value = "30000")
    private int port;

    @TaskParameter(name = "ttl", value = "1")
    private int ttl;

    @TaskParameter(name = "threadCount", value = "1")
    private int threadCount;

    @TaskParameter(name = "maxPackageSize", value = "5000")
    private int maxPackageSize;

    @TaskParameter(name = "mergeTimeout", value = "1000")
    private int mergeTimeout;

    @TaskParameter(name = "sendHeader", value = "false")
    private boolean sendHeader;

    private boolean support;
    private InetAddress inetAddress;
    private MulticastSocket receiveSocket;
    private MulticastSocket sendSocket;
    private List<UDPSendTask> sendTasks;
    private List<UDPReceiveTask> receiveTasks;
    private NetworkInterface networkInterface;
    private Random rand = new Random();

    private ConcurrentHashMap<String, AutoBufAdapter> adapterPool;
    private DoubleKeyMap<String, Integer, FragmentMessage> fragmentMessages = new DoubleKeyMap<>();


    public UDPMulticastSupport(String... name) {
        super(name);
        sendTasks = new ArrayList<>();
        receiveTasks = new ArrayList<>();

        try {
            inetAddress = InetAddress.getByName(ip);
            networkInterface = Sugar.tryGetNetworkInterface(networkInterfaceName);
            if (networkInterface != null) {
                log.info("UDP multicast use " + networkInterface.getName() + " to " + ip);
                for (int i = 0; i < threadCount; i++) {
                    sendTasks.add(ServerManager.me().register(new UDPSendTask()));
                    receiveTasks.add(ServerManager.me().register(new UDPReceiveTask()));
                }
                ServerManager.me().run(this, "removeTimeoutMessage");
                support = true;
            } else {
                support = false;
                log.warning("Unable to find a suitable network interface for UDP multicast!");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /***
     *  绑定组播的话题
     */
    @FilterMessage
    @TaskFunction(name = "bindTopic")
    public void bindTopic(MessageHandle handle) {
        // todo : 白名单,黑名单过滤
        if (adapterPool == null) {
            adapterPool = new ConcurrentHashMap<>();
        }
        if (!handle.getTopic().startsWith("_")) {
            adapterPool.put(handle.getTopic(), new AutoBufAdapter(handle.getDataType()));
            handle.addCallback(this, "distributeMessage", new MessageSourceType[]{MessageSourceType.RAM});
        }
    }

    /***
     *  收到话题数据，随机选取“组播发送”任务，发送消息
     */
    @TaskFunction(name = "distributeMessage")
    public void distributeMessage(Object msg, MessageSource source) {
        String topic = source.getTopic();
        UDPSendTask udpSendTask = sendTasks.get(rand.nextInt(threadCount));
        ServerManager.me().run(udpSendTask, "send", topic, msg, adapterPool.get(topic));
    }

    /***
     * 定时清除组装失败的数据
     */
    @TaskFunction(name = "removeTimeoutMessage")
    public void removeTimeoutMessage() {
        try {
            for (String key : fragmentMessages.getMap().keySet()) {
                ConcurrentHashMap<Integer, FragmentMessage> subMap = fragmentMessages.getMap().get(key);
                for (Map.Entry<Integer, FragmentMessage> entry : subMap.entrySet()) {
                    if (entry.getValue().checkTimeout()) {
                        fragmentMessages.remove(key, entry.getKey());
                    }
                }
            }
        } finally {
            ServerManager.me().run(this, (long) mergeTimeout, "removeTimeoutMessage");
        }
    }

    void stop() {
        for (UDPReceiveTask task : receiveTasks) {
            task.cancel(true);
        }
        // TODO: 添加清除
        this.cancel(false);
    }

    public boolean isSupport() {
        return support;
    }

    @TaskProperties(name = "UDPMulticastSupport::Send", unique = false)
    public class UDPSendTask extends AbstractTask {

        UDPSendTask(String... name) {
            super(name);
            try {
                if (sendSocket == null) {
                    sendSocket = new MulticastSocket();
                    sendSocket.setLoopbackMode(false);
                    sendSocket.setReuseAddress(true);
                    sendSocket.setTimeToLive(ttl);
                    if (networkInterface != null) {
                        sendSocket.setNetworkInterface(networkInterface);
                    }
                    sendSocket.joinGroup(inetAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /***
         * MESSAGE:
         *
         * | IDENTIFIES (String with end 0x00) | TOPIC (String with end 0x00) | SEQ (INT 4bytes [>0] no Fragment [<0] fragment) | --
         * | PAYLOAD_LENGTH (INT 4bytes) | PAYLOAD_FRAME_SIZE (SHORT 2bytes) | PAYLOAD_FRAME_INDEX (SHORT 2bytes) | --
         * | OFFSET (INT 4bytes) | PAYLOAD (n bytes) |
         *
         */
        @TaskFunction(name = "send")
        public void send(String topic, Object msg, AutoBufAdapter adapter) {
            ByteBuilder body;
            if (sendHeader) {
                body = new ByteBuilder(adapter.encode(msg)).resetPosition();
            } else {
                body = new ByteBuilder(adapter.encodeBody(msg)).resetPosition();
            }
            int headerLen = ServerManager.getIdentification().length() + topic.length() + 18;
            boolean needFragment = headerLen + body.getLimit() >= maxPackageSize;
            ByteBuilder data = new ByteBuilder();
            if (needFragment) {
                int contentSize = maxPackageSize - headerLen;
                int frameNumber = calFrameNum(body.getLimit(), contentSize);
                int msgSeq = getSeq(true);

                data.putStringWithTag(ServerManager.getIdentification()); // 标识
                data.putStringWithTag(topic); // topic
                data.putInt(msgSeq); // 序列号
                data.putInt(body.getLimit()); // 内容长度
                data.putShort((short) frameNumber); // 内容帧数
                int tmpPosition = data.getPosition();
                for (int i = 0; i < frameNumber; i++) {
                    data.clearTo(tmpPosition);
                    data.putShort((short) i); // No.
                    data.putInt(i * contentSize); // offset
                    int remain = body.getLimit() - body.getPosition();
                    if (remain < contentSize) {
                        data.putBytes(body.readBytes(remain));
                    } else {
                        data.putBytes(body.readBytes(contentSize));
                    }
                    socketSend(data);
                }
            } else {
                data.putStringWithTag(ServerManager.getIdentification()); // 标识
                data.putStringWithTag(topic); // topic
                data.putInt(getSeq(false)); // 序列号
                data.putInt(body.getLimit()); // 内容长度
                data.putShort((short) 1); // 内容帧数
                data.putShort((short) 0); // No.
                data.putInt(0); // offset
                data.putBytes(body); // 内容
                socketSend(data);
            }

        }

        private void socketSend(ByteBuilder data) {
            DatagramPacket packet = new DatagramPacket(data.toBytes(), 0, data.getLimit(), inetAddress, port);
            try {
                sendSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private int calFrameNum(int total, int frameSize) {
            int result = total / frameSize;
            if (total % frameSize > 0) {
                result++;
            }
            if (result > 65535) {
                throw Sugar.makeThrow("Data length exceeds limit");
            }
            return result;
        }

        private int getSeq(boolean needFragment) {
            seq.compareAndSet(Integer.MAX_VALUE, 0);
            return needFragment ? seq.getAndAdd(1) | 0x80000000 : seq.getAndAdd(1);
        }

    }

    @TaskProperties(name = "UDPMulticastSupport::Receive", unique = false, preemptive = true)
    public class UDPReceiveTask extends AbstractTask {

        public UDPReceiveTask(String... name) {
            super(name);
            try {
                if (receiveSocket == null) {
                    receiveSocket = new MulticastSocket(port);
                    receiveSocket.setLoopbackMode(false);
                    receiveSocket.setReuseAddress(true);
                    receiveSocket.setTimeToLive(ttl);
                    if (networkInterface != null) {
                        receiveSocket.setNetworkInterface(networkInterface);
                    }
                    receiveSocket.joinGroup(inetAddress);
                }
                ServerManager.me().run(this, "receive");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /***
         * MESSAGE:
         *
         * | IDENTIFIES (String with end 0x00) | TOPIC (String with end 0x00) | SEQ (INT 4bytes [>0] no Fragment [<0] fragment) | --
         * | PAYLOAD_LENGTH (INT 4bytes) | PAYLOAD_FRAME_SIZE (SHORT 2bytes) | PAYLOAD_FRAME_INDEX (SHORT 2bytes) | --
         * | OFFSET (INT 4bytes) | PAYLOAD (n bytes) |
         *
         */
        @TaskFunction(name = "receive")
        public void receive() {
            DatagramPacket packet = new DatagramPacket(new byte[maxPackageSize], maxPackageSize);
            try {
                receiveSocket.receive(packet);
                ByteBuilder bb = new ByteBuilder(Arrays.copyOf(packet.getData(), packet.getLength())).resetPosition();
                String id = bb.readStringWithTag();
                if (!ServerManager.getIdentification().equals(id)) { // 过滤内部回环消息
                    String topic = bb.readStringWithTag();
                    int seq = bb.readInt();
                    int payloadLength = bb.readInt();
                    int payloadFrameSize = Unsigned.unsignedShort2Int(bb.readShort());
                    int frameIndex = Unsigned.unsignedShort2Int(bb.readShort());
                    int offset = bb.readInt();

                    if (seq < 0) {
                        FragmentMessage fm = fragmentMessages.get(id, seq);
                        if (fm == null) {
                            fm = new FragmentMessage(topic, payloadLength, payloadFrameSize);
                            fragmentMessages.put(id, seq, fm);
                        }
                        boolean result = fm.fillData(topic, payloadLength, payloadFrameSize, frameIndex, offset, bb.readBytes(bb.getLimit() - bb.getPosition()));
                        if (result && fm.checkFinish()) {
                            pushData(topic, fm.getData(), new MessageSource(id, topic, MessageSourceType.UDP_MULTICAST, packet.getAddress().getHostAddress(), packet.getPort()));
                            fragmentMessages.remove(id, seq);
                        }
                    } else {
                        pushData(topic, bb.readBytes(bb.getLimit() - bb.getPosition()), new MessageSource(id, topic, MessageSourceType.UDP_MULTICAST, packet.getAddress().getHostAddress(), packet.getPort()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ServerManager.me().run(this, "receive");
            }
        }


        private void pushData(String topic, byte[] bytes, MessageSource source) {
            MessageHandle handle = ServerManager.me().getMessageHandle(topic);
            if (handle != null) {
                AutoBufAdapter adapter = adapterPool.getOrDefault(topic, null);
                if (adapter == null) {
                    adapter = new AutoBufAdapter(handle.getDataType());
                    adapterPool.put(topic, adapter);
                }

                Object msg;
                if (sendHeader) {
                    msg = adapter.decode(bytes);
                } else {
                    msg = adapter.decodeBody(bytes);
                }
                handle.pushSubData(msg, source);
            }
        }
    }

    public class FragmentMessage {
        private String topic;
        private int payloadLength;
        private int payloadFrameNumber;
        private boolean[] fillFlag;
        private byte[] data;
        private long lastUpdateTime;

        public FragmentMessage(String topic, int payloadLength, int payloadFrameNumber) {
            this.topic = topic;
            this.payloadLength = payloadLength;
            this.payloadFrameNumber = payloadFrameNumber;
            this.fillFlag = new boolean[payloadFrameNumber];
            this.data = new byte[payloadLength];
            this.lastUpdateTime = System.currentTimeMillis();
            Arrays.fill(fillFlag, false);
        }

        public boolean fillData(String topic, int payloadLength, int payloadFrameNumber, int frameIndex, int offset, byte[] subData) {
            if (!this.topic.equals(topic) || this.payloadLength != payloadLength || this.payloadFrameNumber != payloadFrameNumber) {
                return false;
            }

            if (frameIndex < payloadFrameNumber && (!fillFlag[frameIndex]) && ((offset + subData.length) <= payloadLength)) {
                System.arraycopy(subData, 0, data, offset, subData.length);
                this.fillFlag[frameIndex] = true;
                lastUpdateTime = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        }

        public boolean checkFinish() {
            for (int i = payloadFrameNumber - 1; i >= 0; i--) {
                if (!this.fillFlag[i]) {
                    return false;
                }
            }
            return true;
        }

        public boolean checkTimeout() {
            return System.currentTimeMillis() - this.lastUpdateTime > mergeTimeout;
        }

        public byte[] getData() {
            return data;
        }

        public String getTopic() {
            return topic;
        }

        @Override
        public String toString() {
            return "FragmentMessage{" + "topic='" + topic + '\'' + ", payloadLength=" + payloadLength + ", payloadFrameNumber=" + payloadFrameNumber + ", fillFlag=" + Arrays.toString(fillFlag) + ", data=" + Arrays.toString(data) + ", lastUpdateTime=" + lastUpdateTime + '}';
        }
    }

}
