package cn.autolabor.core.server;

import cn.autolabor.core.server.config.ConfigServer;
import cn.autolabor.core.server.executor.AbstractTask;
import cn.autolabor.core.server.executor.TaskExecutorServer;
import cn.autolabor.core.server.executor.TaskThreadLocal;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.core.server.message.MessageServer;
import cn.autolabor.core.server.task.TaskServer;
import cn.autolabor.module.communication.TCPClientSupport;
import cn.autolabor.module.communication.TCPRequest;
import cn.autolabor.module.communication.TCPRespStatusType;
import cn.autolabor.module.communication.TCPResponse;
import cn.autolabor.module.networkhub.TCPDialogClient;
import cn.autolabor.util.Strings;
import cn.autolabor.util.lambda.LambdaFunWithName;
import cn.autolabor.util.reflect.TypeNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerManager {

    private static Map<String, Object> systemConfig = new ConcurrentHashMap<>();
    private static AtomicBoolean init = new AtomicBoolean(false);

    static {
        systemConfig.put("TrackBehavior", true);
        systemConfig.put("Identification", Strings.getShortUUID());
        systemConfig.put("Setup", new DefaultSetup());
    }

    TaskServer taskServer;
    ConfigServer configServer;
    MessageServer messageServer;
    TaskExecutorServer executorServer;

    private ServerManager() {
        configServer = new ConfigServer();
        executorServer = new TaskExecutorServer(4);
        taskServer = new TaskServer();
        messageServer = new MessageServer();
    }

    public static void setSystemConfig(String key, Object value) {
        systemConfig.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSystemConfig(String key, T defaultValue) {
        return (T) systemConfig.getOrDefault(key, defaultValue);
    }

    public static String getIdentification() {
        return (String) systemConfig.get("Identification");
    }

    public static void setIdentification(String identification) {
        systemConfig.put("Identification", identification);
    }

    public static void setSetup(Setup setup) {
        systemConfig.put("Setup", setup);
    }

    public static ServerManager me() {
        ServerManager manager = Singleton.INSTANCE.getInstance();

        if (init.compareAndSet(false, true)) {
            Setup setup = (Setup) systemConfig.getOrDefault("Setup", null);
            if (setup != null) {
                setup.start();
                Runtime.getRuntime().addShutdownHook(new Thread(setup::stop));
            }
        }

        return manager;
    }

    public void loadConfig(String filePath) {
        this.configServer.load(filePath);
    }

    public boolean checkConfig(AbstractTask task, String paramName) {
        return this.configServer.checkParam(task.getTaskName(), paramName);
    }

    public boolean checkConfig(String key, String paramName) {
        return this.configServer.checkParam(key, paramName);
    }

    public Object getConfig(AbstractTask task, String paramName) {
        return this.configServer.getParam(task.getTaskName(), paramName);
    }

    public Object getConfig(String key, String paramName) {
        return this.configServer.getParam(key, paramName);
    }

    public void setConfig(AbstractTask task, String paramName, Object param) {
        task.updateParameter(paramName, param);
        this.configServer.setParam(task.getTaskName(), paramName, param);
    }

    public void setConfig(String key, String paramName, Object param) {
        AbstractTask task = taskServer.getTaskByName("key");
        if (null != task) {
            task.updateParameter(paramName, param);
        }
        this.configServer.setParam(key, paramName, param);
    }

    public void showConfig() {
        System.out.println(this.configServer.dump());
    }

    public MessageHandle getOrCreateMessageHandle(String topic, TypeNode type) {
        return this.messageServer.getOrCreateMessageHandle(topic, type);
    }

    public MessageHandle getMessageHandle(String topic) {
        return this.messageServer.getMessageHandle(topic);
    }

    public void addCreateHandleCallback(AbstractTask task, String event) {
        this.messageServer.addCreateHandleCallback(task, event);
    }

    public void removeCreateHandleCallback(AbstractTask task, String event) {
        this.messageServer.removeCreateHandleCallback(task, event);
    }

    public void addRegisterTaskCallback(AbstractTask task, String event) {
        this.taskServer.addRegisterTaskCallback(task, event);
    }

    public void removeRegisterTaskCallback(AbstractTask task, String event) {
        this.taskServer.removeRegisterTaskCallback(task, event);
    }

    public boolean activationTask(AbstractTask task) {
        return taskServer.activation(task);
    }

    public <T extends AbstractTask> T register(T task) {
        taskServer.register(task);
        executorServer.tryStart(task);
        if (task.isNeedTrackBehavior()) {
            TaskThreadLocal.unmarkTask(task);
        }
        return task;
    }

    public <T extends AbstractTask> T unRegister(T task) {
        task.suspend = true;
        task.onClose();
        messageServer.remove(task);
        taskServer.remove(task);
        return null;
    }

    public AbstractTask getTaskByName(String taskName) {
        return taskServer.getTaskByName(taskName);
    }

    public void run(AbstractTask task, String methodName, Object... params) {
        this.run(task, TimeUnit.NANOSECONDS, 0L, methodName, params);
    }

    public void run(AbstractTask task, Long delay, String methodName, Object... params) {
        this.run(task, TimeUnit.MILLISECONDS, delay, methodName, params);
    }

    public void run(AbstractTask task, TimeUnit timeUnit, Long delay, String methodName, Object... params) {
        if (executorServer.isRunning()) {
            executorServer.schedule(task, System.nanoTime() + TimeUnit.NANOSECONDS.convert(delay, timeUnit), methodName, params);
        }
    }

    public void delayRun(AbstractTask task, Long delay, String methodName, Object... params) {
        if (executorServer.isRunning()) {
            long executeTime = task.getTime();
            executorServer.schedule(task, ((executeTime > System.nanoTime() || executeTime < 0) ? System.nanoTime() : executeTime) + TimeUnit.NANOSECONDS.convert(delay, TimeUnit.MILLISECONDS), methodName, params);
        }
    }

    public void run(AbstractTask task, LambdaFunWithName fun, Object... params) {
        this.run(task, TimeUnit.NANOSECONDS, 0L, fun, params);
    }

    public void run(AbstractTask task, Long delay, LambdaFunWithName fun, Object... params) {
        this.run(task, TimeUnit.MILLISECONDS, delay, fun, params);
    }

    public void run(AbstractTask task, TimeUnit timeUnit, Long delay, LambdaFunWithName fun, Object... params) {
        if (executorServer.isRunning()) {
            executorServer.schedule(task, System.nanoTime() + TimeUnit.NANOSECONDS.convert(delay, timeUnit), fun, params);
        }
    }

    public void delayRun(AbstractTask task, Long delay, LambdaFunWithName fun, Object... params) {
        if (executorServer.isRunning()) {
            long executeTime = task.getTime();
            executorServer.schedule(task, ((executeTime > System.nanoTime() || executeTime < 0) ? System.nanoTime() : executeTime) + TimeUnit.NANOSECONDS.convert(delay, TimeUnit.MILLISECONDS), fun, params);
        }
    }

    public void stop() {
        Setup setup = (Setup) systemConfig.getOrDefault("Setup", null);
        if (setup != null) {
            setup.stop();
        }
        // TODO: 其他service清除信息
        executorServer.shutdown();
    }

    public TCPResponse call(String userId, String taskName, String eventName, Object... params) {
        if (getIdentification().equals(userId)) {
            return localCall(taskName, eventName, params);
        } else {
            return remoteCall(userId, taskName, eventName, params);
        }
    }

    public TCPResponse remoteCall(String userId, String taskName, String eventName, Object... params) {
        TCPResponse response = null;
        TCPRequest request = new TCPRequest(taskName, eventName);
        for (Object o : params) {
            request.addParam(o);
        }
        if (this.getTaskByName("TCPClientSupport") != null || this.getTaskByName("TCPServiceSupport") != null) {
            response = TCPClientSupport.callOne(userId, request);
        } else if (this.getTaskByName("TCPDialogServer") != null || this.getTaskByName("TCPDialogClient") != null) {
            response = TCPDialogClient.callOne(userId, request);
        }
        if (response == null) {
            return new TCPResponse(TCPRespStatusType.NO_DEVICE, null);
        } else {
            return response;
        }
    }

    public TCPResponse localCall(String taskName, String eventName, Object... params) {
        AbstractTask task = this.getTaskByName(taskName);
        if (task != null) {
            try {
                return new TCPResponse(TCPRespStatusType.SUCCESS, task.syncRun(eventName, params));
            } catch (Exception e) {
                return new TCPResponse(TCPRespStatusType.NO_METHOD, null);
            }
        } else {
            return new TCPResponse(TCPRespStatusType.NO_TASK, null);
        }
    }

    public StackTraceElement getTaskTrace() {
        StackTraceElement[] stackInfo = Thread.currentThread().getStackTrace();
        //        for (StackTraceElement stackTraceElement : stackInfo) {
        //            System.err.println(stackTraceElement);
        //        }
        for (StackTraceElement stackTraceElement : stackInfo) {
            if (taskServer.taskTypeNames.contains(stackTraceElement.getClassName())) {
                return stackTraceElement;
            }
        }
        return null;
    }

    public void dump() {
        System.out.println("===TASK===");
        System.out.println(taskServer);
        System.out.println("===CONFIG===");
        System.out.println(configServer);
    }

    private enum Singleton {
        INSTANCE;

        private ServerManager manager;

        Singleton() {
            manager = new ServerManager();
        }

        public ServerManager getInstance() {
            return manager;
        }
    }

}
