package cn.autolabor.core.server;

import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.module.networkhub.TCPDialogClient;
import cn.autolabor.module.networkhub.TCPDialogServer;
import cn.autolabor.module.networkhub.UDPMulticastBroadcaster;
import cn.autolabor.module.networkhub.UDPMulticastReceiver;

public class DefaultSetup implements Setup {

    @Override
    public void start() {
        ServerManager.me().register(new UDPMulticastBroadcaster());
        ServerManager.me().register(new UDPMulticastReceiver());
        ServerManager.me().register(new TCPDialogServer());
        TCPDialogClient.startYell();
    }

    @Override
    public void stop() {
        ServerManager.me().taskServer.getAllTasks().forEach(AbstractTask::onClose);
        System.out.println("\n" +
                        " █████╗ ██╗   ██╗████████╗ ██████╗ ██╗      █████╗ ██████╗  ██████╗ ██████╗                   \n" +
                        "██╔══██╗██║   ██║╚══██╔══╝██╔═══██╗██║     ██╔══██╗██╔══██╗██╔═══██╗██╔══██╗                  \n" +
                        "███████║██║   ██║   ██║   ██║   ██║██║     ███████║██████╔╝██║   ██║██████╔╝                  \n" +
                        "██╔══██║██║   ██║   ██║   ██║   ██║██║     ██╔══██║██╔══██╗██║   ██║██╔══██╗                  \n" +
                        "██║  ██║╚██████╔╝   ██║   ╚██████╔╝███████╗██║  ██║██████╔╝╚██████╔╝██║  ██║                  \n" +
                        "╚═╝  ╚═╝ ╚═════╝    ╚═╝    ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝                  \n" +
                        "                ███████╗██████╗  █████╗ ███╗   ███╗███████╗██╗    ██╗ ██████╗ ██████╗ ██╗  ██╗\n" +
                        "                ██╔════╝██╔══██╗██╔══██╗████╗ ████║██╔════╝██║    ██║██╔═══██╗██╔══██╗██║ ██╔╝\n" +
                        "                █████╗  ██████╔╝███████║██╔████╔██║█████╗  ██║ █╗ ██║██║   ██║██████╔╝█████╔╝ \n" +
                        "                ██╔══╝  ██╔══██╗██╔══██║██║╚██╔╝██║██╔══╝  ██║███╗██║██║   ██║██╔══██╗██╔═██╗ \n" +
                        "                ██║     ██║  ██║██║  ██║██║ ╚═╝ ██║███████╗╚███╔███╔╝╚██████╔╝██║  ██║██║  ██╗\n" +
                        "                ╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝ ╚══╝╚══╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝");
    }
}
