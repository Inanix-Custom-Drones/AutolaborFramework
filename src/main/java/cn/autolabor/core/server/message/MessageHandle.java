package cn.autolabor.core.server.message;

import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.CallbackItem;
import cn.autolabor.core.server.executor.TaskMethod;
import cn.autolabor.core.server.executor.TaskThreadLocal;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.reflect.TypeNode;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MessageHandle<T> {
    private static int DEFAULT_DATA_LENGTH = 10;

    private ReentrantLock lock = new ReentrantLock();
    private String topic;
    private int maxSize;
    private TypeNode dataType;
    private Deque<T> dataList;
    private Map<CallbackItem, EventInfo> callbackFunction; // method name  :   send source
    private MessageServer server;
    private long lastMessageReceiveTime = -1;

    MessageHandle(String topic, TypeNode dataType, MessageServer server) {
        this(topic, dataType, server, DEFAULT_DATA_LENGTH);
    }

    MessageHandle(String topic, TypeNode dataType, MessageServer server, int maxSize) {
        this.topic = topic;
        this.server = server;
        this.maxSize = maxSize;
        this.dataType = dataType;
        this.dataList = new ArrayDeque<>();
        this.callbackFunction = new HashMap<>();
    }

    public void pushSubData(T data) {
        pushSubData(data, new MessageSource(ServerManager.getIdentification(), topic, MessageSourceType.RAM, null, null));
    }

    public void pushSubData(T data, MessageSource source) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            lastMessageReceiveTime = System.currentTimeMillis();
            dataList.addFirst(data);
            while (this.dataList.size() > maxSize) {
                dataList.removeLast();
            }
            TaskThreadLocal.markTopic(this);
            for (Map.Entry<CallbackItem, EventInfo> entry : callbackFunction.entrySet()) {
                if (entry.getValue().checkSourceType(source.getSourceType())) {
                    if (entry.getValue().needSource) {
                        ServerManager.me().run(entry.getKey().getTask(), entry.getKey().getEvent(), data, source);
                    } else {
                        ServerManager.me().run(entry.getKey().getTask(), entry.getKey().getEvent(), data);
                    }
                }
            }
            TaskThreadLocal.unmarkTopic();
        } finally {
            lock.unlock();
        }
    }

    public T getFirstData() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.dataList.size() > 0) {
                return this.dataList.getFirst();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public List<T> getAllData() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return new ArrayList<>(dataList);
        } finally {
            lock.unlock();
        }
    }

    public long getLastMessageReceiveTime() {
        return this.lastMessageReceiveTime;
    }

    public boolean addCallback(AbstractTask task, String event, MessageSourceType[] sourceFilter) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            TaskMethod method = task.getEvent(event);
            TypeNode[] params = method.getParamsType();
            if (params != null && params.length == 1 && Sugar.checkInherit(dataType.getRawType(), params[0].getRawType())) {
                return putCallbackFunction(task, event, false, sourceFilter);
            } else if (params != null && params.length == 2 && Sugar.checkInherit(dataType.getRawType(), params[0].getRawType()) && params[1].getRawType() == MessageSource.class) {
                return putCallbackFunction(task, event, true, sourceFilter);
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Set<CallbackItem> getCallbackFunctionItems() {
        return callbackFunction.keySet();
    }

    public void removeCallbackByTask(AbstractTask task) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Set<CallbackItem> keys = getCallbackFunctionItems();
            for (CallbackItem item : keys) {
                if (item.getTask().equals(task)) {
                    callbackFunction.remove(item);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public TypeNode getDataType() {
        return dataType;
    }

    public String getTopic() {
        return topic;
    }

    private boolean putCallbackFunction(AbstractTask task, String event, boolean needSource, MessageSourceType[] sourceFilter) {
        CallbackItem callbackItem = new CallbackItem(task, event);
        if (!callbackFunction.containsKey(callbackItem)) {
            callbackFunction.put(callbackItem, new EventInfo(needSource, sourceFilter));
        }
        return true;
    }

    private class EventInfo {
        boolean needSource;
        Set<MessageSourceType> sourceFilter;

        public EventInfo(boolean needSource, MessageSourceType[] sourceFilter) {
            this.needSource = needSource;
            if (sourceFilter != null) {
                this.sourceFilter = new HashSet<>(Arrays.asList(sourceFilter));
            } else {
                this.sourceFilter = new HashSet<>();
            }
        }

        public boolean isNeedSource() {
            return needSource;
        }

        public Set<MessageSourceType> getSourceFilter() {
            return sourceFilter;
        }

        public boolean checkSourceType(MessageSourceType type) {

            if (sourceFilter.size() == 0) {
                return true;
            } else {
                return sourceFilter.contains(type);
            }


        }
    }
}
