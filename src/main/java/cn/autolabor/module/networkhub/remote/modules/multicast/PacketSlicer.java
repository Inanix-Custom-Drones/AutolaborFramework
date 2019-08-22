package cn.autolabor.module.networkhub.remote.modules.multicast;

import cn.autolabor.module.networkhub.dependency.AbstractDependent;
import cn.autolabor.module.networkhub.dependency.Component;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;
import cn.autolabor.module.networkhub.remote.utilities.SimpleInputStream;
import cn.autolabor.module.networkhub.remote.utilities.SimpleOutputStream;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class PacketSlicer
        extends AbstractDependent<PacketSlicer>
        implements MulticastListener {

    private static final List<Byte> INTEREST_SET = new ArrayList<>(1);

    static {
        INTEREST_SET.add(UdpCmd.PACKET_SLICE.id);
    }

    private final AtomicLong sequence = new AtomicLong(0);
    private final ConcurrentHashMap<PackInfo, Buffer> buffers = new ConcurrentHashMap<>();
    private final List<MulticastListener> listeners = new LinkedList<>();

    public PacketSlicer() {
        super(PacketSlicer.class);
    }

    @Override
    public boolean sync(Component dependency) {
        super.sync(dependency);
        if (!dependency.equals(this) && dependency instanceof MulticastListener)
            listeners.add((MulticastListener) dependency);
        return false;
    }

    @Override
    public Collection<Byte> getInterest() {
        return INTEREST_SET;
    }


    /**
     * 使用分片协议广播一包
     *
     * @param cmd     实际指令
     * @param payload 实际数据
     * @param size    分片数据长度
     * @param output  发布方法
     */
    void broadcast(
            byte cmd,
            byte[] payload,
            int size,
            Consumer<byte[]> output
    ) {
        SimpleInputStream stream = new SimpleInputStream(payload);
        SimpleOutputStream buffer = new SimpleOutputStream(9);

        byte[] s = buffer.zigzag(sequence.incrementAndGet(), false).toArray();
        buffer.ptr = 0;

        long index = 0;

        while (stream.available() > 0) {
            // 编码子包序号
            byte[] i = buffer.zigzag(index++, false).toArray();
            buffer.ptr = 0;
            // 如果是最后一包，应该多长?
            int last = stream.available() + 2 + s.length + i.length;

            SimpleOutputStream out;
            // 打包
            if (last <= size) {
                out = new SimpleOutputStream(last);
                out.write(0); // 空一位作为停止位
                out.write(cmd);// 保存实际指令
            } else {
                out = new SimpleOutputStream(size);
            }

            out.write(s);
            out.write(i);
            out.writeFrom(stream, out.available());
            output.accept(out.core);
        }
    }

    @Override
    public void process(String sender, UdpCmd cmd, byte[] payload) {
        SimpleInputStream stream = new SimpleInputStream(payload); // 构造流

        Byte command = null;
        if (stream.look() == 0) {
            int skip = stream.read();
            command = (byte) stream.read();
        }

        long subSeq = stream.zigzag(false); // 解子包序列号
        long index = stream.zigzag(false);  // 解子包序号
        byte[] rest = stream.lookRest();    // 解子包负载

        Pack pack;
        if (index == 0L && command != null) {
            pack = new Pack(command, rest);
        } else {
            PackInfo key = new PackInfo(sender, subSeq);
            pack = buffers
                    .computeIfAbsent(key, it -> new Buffer())
                    .put(command, (int) index, rest);
            if (pack != null)
                buffers.remove(key);
        }

        if (pack == null)
            return;

        final UdpCmd xxx = UdpCmd.memory.get(pack.cmd);

        listeners
                .stream()
                .filter(it -> it.getInterest().isEmpty() || it.getInterest().contains(xxx.id))
                .forEach(it -> it.process(sender, xxx, pack.payload));
    }

    /**
     * 清理缓冲中最后活跃时间超过 [timeout]ms 的数据包
     */
    public void refresh(int timeout) {
        long now = System.currentTimeMillis();
        buffers // 删除超时包
                .entrySet()
                .stream()
                .filter(it -> it.getValue().by(now) > timeout)
                .forEach(it -> buffers.remove(it.getKey()));
    }

    private final static class PackInfo {
        final String name;
        final long seqence;

        PackInfo(String name, long seqence) {
            this.name = name;
            this.seqence = seqence;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof PackInfo))
                return false;

            PackInfo temp = (PackInfo) obj;
            return temp.name.equals(name) && temp.seqence == seqence;
        }

        @Override
        public int hashCode() {
            return name.hashCode() * 31 + (int) seqence;
        }
    }

    private final static class Pack {
        final byte cmd;
        final byte[] payload;

        Pack(byte cmd, byte[] payload) {
            this.cmd = cmd;
            this.payload = payload;
        }
    }

    /**
     * 子包缓存
     */
    private static class Buffer {
        private final List<Hook> list = new LinkedList<>();
        private final HashMap<Integer, Hook> mark = new HashMap<>();
        private long time = System.currentTimeMillis();
        private Byte command = null;

        /**
         * @return 最后活跃时间到当前的延时
         */
        long by(long now) {
            return now - time;
        }

        /**
         * 置入一个小包
         *
         * @param cmd     包指令
         * @param index   序号
         * @param payload 负载
         * @return 已完结则返回完整包
         */
        Pack put(
                Byte cmd,
                Integer index,
                byte[] payload
        ) {
            // 修改状态，加锁保护
            synchronized (list) {
                if (command != null) {
                    mark.remove(index).ptr = payload;
                } else {
                    command = cmd;

                    for (int i = list.size(); i < index; ++i) {
                        Hook hook = new Hook();
                        mark.put(i, hook);
                        list.add(hook);
                    }

                    if (list.size() != index)
                        mark.remove(index).ptr = payload;
                    else
                        list.add(new Hook(payload));
                }
            }

            // 已经保存最后一包并且不缺包
            if (command != null && mark.isEmpty()) {
                int size = list.stream().mapToInt(it -> it.ptr.length).sum();
                SimpleOutputStream stream = new SimpleOutputStream(size);
                list.stream().map(it -> it.ptr).forEach(stream::write);
                return new Pack(command, stream.core);
            }

            // 更新最后活跃时间
            time = System.currentTimeMillis();
            return null;
        }

        private static final class Hook {
            byte[] ptr;

            Hook() {
            }

            Hook(byte[] ptr) {
                this.ptr = ptr;
            }
        }
    }
}
