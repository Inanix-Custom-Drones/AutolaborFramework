package cn.autolabor.module.networkhub.remote.modules.multicast;

import cn.autolabor.module.networkhub.dependency.Component;
import cn.autolabor.module.networkhub.remote.resources.UdpCmd;

import java.util.Collection;

public interface MulticastListener extends Component {
    Collection<Byte> getInterest();

    void process(String sender, UdpCmd cmd, byte[] payload);
}
