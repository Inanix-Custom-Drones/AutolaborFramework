package cn.autolabor.core.server.executor;

import java.util.concurrent.TimeUnit;

public interface ScheduledItemInterface {

    long getTime();

    long getDelay(TimeUnit unit);

    boolean isTimeout();

}
