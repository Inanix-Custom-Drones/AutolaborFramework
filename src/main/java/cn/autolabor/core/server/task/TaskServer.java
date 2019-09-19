package cn.autolabor.core.server.task;

import cn.autolabor.core.annotation.FilterMessage;
import cn.autolabor.core.annotation.FilterTask;
import cn.autolabor.core.annotation.SubscribeMessage;
import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.CallbackItem;
import cn.autolabor.core.server.executor.SimpleTask;
import cn.autolabor.core.server.executor.TaskMethod;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.lambda.LambdaFunWithName;
import cn.autolabor.util.reflect.Reflects;
import cn.autolabor.util.reflect.TypeNode;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class TaskServer {

    public Set<String> taskTypeNames = new HashSet<>();

    private final ReentrantLock lock = new ReentrantLock();
    private Set<CallbackItem> registerTaskCallback = new HashSet<>();
    private ConcurrentMap<String, AbstractTask> taskPool;

    public TaskServer() {
        this.taskPool = new ConcurrentHashMap<>();
    }

    public boolean activation(AbstractTask task) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 判断任务是否已经被注册
            if (task.register) {
                return false;
            }

            // 查询是否有相同key的任务, 如果有抛出异常
            if (taskPool.containsKey(task.getTaskName())) {
                return false;
            }

            // 查询任务是否唯一并且判断其唯一性
            if (task.isUnique() && containTaskByType(task.getClass())) {
                return false;
            }
            taskPool.put(task.getTaskName(), task);
            if (!task.getClass().equals(SimpleTask.class)) {
                taskTypeNames.add(task.getClass().getName());
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void remove(AbstractTask task) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            taskPool.remove(task.getTaskName());
            Set<CallbackItem> remove = new HashSet<>();
            registerTaskCallback.forEach(i -> {
                if (i.getTask().equals(task)) {
                    remove.add(i);
                }
            });
            registerTaskCallback.removeAll(remove);
        } finally {
            lock.unlock();
        }
    }

    public void register(AbstractTask task) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (!task.silentInit) {
                subscribeMessage(task);
                // 把自己介绍给其他task
                for (CallbackItem callback : registerTaskCallback) {
                    ServerManager.me().run(callback.getTask(), callback.getEvent(), task);
                }
                // 再了解其他task
                filterTask(task);
                filterMessage(task);
            }
            task.register = true;
        } finally {
            lock.unlock();
        }

    }

    private void subscribeMessage(AbstractTask task) {
        Method[] methods = task.getClass().getMethods();
        for (Method m : methods) {
            SubscribeMessage t = m.getAnnotation(SubscribeMessage.class);
            if (t != null) {
                if (m.getParameterCount() == 1 || m.getParameterCount() == 2) {
                    Type paramType = m.getParameters()[0].getParameterizedType();
                    String topicStr = t.topic();
                    if (topicStr.startsWith("${") && topicStr.endsWith("}")) {
                        topicStr = (String) ServerManager.me().getConfig(task, topicStr.substring(2, topicStr.length() - 1));
                    }
                    MessageHandle handle = ServerManager.me().getOrCreateMessageHandle(topicStr, new TypeNode(paramType));
                    TaskFunction tf = m.getAnnotation(TaskFunction.class);
                    if (tf != null) {
                        handle.addCallback(task, Strings.isBlank(tf.name()) ? m.getName() : tf.name(), t.source());
                    } else {
                        throw Sugar.makeThrow("Method %s must have TaskFunction annotation", m.getName());
                    }
                } else {
                    throw Sugar.makeThrow("Method %s has more than 2 parameter", m.getName());
                }
            }
        }
    }

    private void filterMessage(AbstractTask task) {
        Method[] methods = task.getClass().getMethods();
        for (Method m : methods) {
            FilterMessage t = m.getAnnotation(FilterMessage.class);
            if (t != null) {
                if (Reflects.checkMethodParameter(m, MessageHandle.class)) {
                    TaskFunction tf = m.getAnnotation(TaskFunction.class);
                    if (tf != null) {
                        ServerManager.me().addCreateHandleCallback(task, Strings.isBlank(tf.name()) ? m.getName() : tf.name());
                    } else {
                        throw Sugar.makeThrow("Method %s must have TaskFunction annotation", m.getName());
                    }
                } else {
                    throw Sugar.makeThrow("Method %s parameter type error", m.getName());
                }
            }
        }
    }

    private void filterTask(AbstractTask task) {
        Method[] methods = task.getClass().getMethods();
        for (Method m : methods) {
            FilterTask t = m.getAnnotation(FilterTask.class);
            if (t != null) {
                if (Reflects.checkMethodParameter(m, AbstractTask.class)) {
                    TaskFunction tf = m.getAnnotation(TaskFunction.class);
                    if (tf != null) {
                        addRegisterTaskCallback(task, Strings.isBlank(tf.name()) ? m.getName() : tf.name());
                    } else {
                        throw Sugar.makeThrow("Method %s must have TaskFunction annotation", m.getName());
                    }
                } else {
                    throw Sugar.makeThrow("Method %s parameter type error", m.getName());
                }
            }
        }
    }

    public AbstractTask getTaskByName(String taskName) {
        return this.taskPool.getOrDefault(taskName, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractTask> Map<String, T> getTaskByType(Class<T> type) {
        Map<String, T> map = new HashMap<>();
        for (Map.Entry<String, AbstractTask> entry : taskPool.entrySet()) {
            if (type.isInstance(entry.getValue())) {
                map.put(entry.getKey(), (T) entry.getValue());
            }
        }
        return map;
    }

    public boolean containTaskByType(Class type) {
        for (Map.Entry<String, AbstractTask> entry : taskPool.entrySet()) {
            if (entry.getValue().getClass() == type) {
                return true;
            }
        }
        return false;
    }

    public void addRegisterTaskCallback(AbstractTask task, String eventName) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            TaskMethod method = task.getEvent(eventName);
            if (null != method) {
                CallbackItem event = new CallbackItem(task, new LambdaFunWithName(method.getMethodName(), method.getFun()));
                if (!registerTaskCallback.contains(event)) {
                    registerTaskCallback.add(event);
                    for (Map.Entry<String, AbstractTask> entry : taskPool.entrySet()) {
                        ServerManager.me().run(task, eventName, entry.getValue()); // topic , messageHandle
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeRegisterTaskCallback(AbstractTask task, String eventName) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            TaskMethod method = task.getEvent(eventName);
            if (null != method) {
                CallbackItem event = new CallbackItem(task, new LambdaFunWithName(method.getMethodName(), method.getFun()));
                registerTaskCallback.remove(event);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getTaskCount() {
        return this.taskPool.size();
    }

    public List<String> getTaskNames() {
        return new ArrayList<>(this.taskPool.keySet());
    }

    public List<String> getTaskEvents(String taskName) {
        AbstractTask task = getTaskByName(taskName);
        if (task != null) {
            return task.getAllEventsName();
        }
        return new ArrayList<>();
    }

    public List<AbstractTask> getAllTasks() {
        return new ArrayList<>(taskPool.values());
    }

    @Override
    public String toString() {
        return "TaskServer{" + "taskPool=" + taskPool + '}';
    }
}
