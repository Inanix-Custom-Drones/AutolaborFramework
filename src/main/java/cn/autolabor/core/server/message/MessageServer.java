package cn.autolabor.core.server.message;

import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.CallbackItem;
import cn.autolabor.core.server.executor.TaskMethod;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.lambda.LambdaFunWithName;
import cn.autolabor.util.reflect.TypeNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class MessageServer {

    private ConcurrentMap<String, MessageHandle> messagePool;

    private Set<CallbackItem> createHandleCallback = new HashSet<>();

    private ReentrantLock lock = new ReentrantLock();

    public MessageServer() {
        this.messagePool = new ConcurrentHashMap<>();
    }


    @SuppressWarnings("unchecked")
    public MessageHandle getOrCreateMessageHandle(String topic, TypeNode type) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            MessageHandle messageHandle = messagePool.getOrDefault(topic, null);
            if (messageHandle == null) {
                messageHandle = new MessageHandle(topic, type, this);
                messagePool.put(topic, messageHandle);

                for (CallbackItem callback : createHandleCallback) {
                    ServerManager.me().run(callback.getTask(), callback.getEvent(), messageHandle); // topic , messageHandle
                }

            } else if (!messageHandle.getDataType().equals(type)) {
                throw Sugar.makeThrow("Topic %s does not match the type %s", topic, type.toString());
            }
            return messageHandle;
        } finally {
            lock.unlock();
        }
    }

    public MessageHandle getMessageHandle(String topic) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return messagePool.getOrDefault(topic, null);
        } finally {
            lock.unlock();
        }
    }

    public void addCreateHandleCallback(AbstractTask task, String eventName) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            TaskMethod method = task.getEvent(eventName);
            if (null != method) {
                CallbackItem event = new CallbackItem(task, new LambdaFunWithName(method.getMethodName(), method.getFun()));
                if (!createHandleCallback.contains(event)) {
                    createHandleCallback.add(event);
                    for (Map.Entry<String, MessageHandle> entry : messagePool.entrySet()) {
                        ServerManager.me().run(task, eventName, entry.getValue()); //  messageHandle
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeCreateHandleCallback(AbstractTask task, String eventName) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            TaskMethod method = task.getEvent(eventName);
            if (null != method) {
                CallbackItem event = new CallbackItem(task, new LambdaFunWithName(method.getMethodName(), method.getFun()));
                createHandleCallback.remove(event);
            }
        } finally {
            lock.unlock();
        }
    }

    public void remove(AbstractTask task) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Set<CallbackItem> remove = new HashSet<>();
            createHandleCallback.forEach(i -> {
                if (i.getTask().equals(task)) {
                    remove.add(i);
                }
            });
            createHandleCallback.removeAll(remove);

            messagePool.values().forEach(i -> {
                i.removeCallbackByTask(task);
            });
        } finally {
            lock.unlock();
        }
    }

    public int getTopicCount() {
        return this.messagePool.size();
    }
}
