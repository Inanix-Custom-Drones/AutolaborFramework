package cn.autolabor.core.server.executor;

import cn.autolabor.core.annotation.InjectMessage;
import cn.autolabor.core.annotation.TaskFunction;
import cn.autolabor.core.annotation.TaskParameter;
import cn.autolabor.core.annotation.TaskProperties;
import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.core.server.statistics.*;
import cn.autolabor.util.Strings;
import cn.autolabor.util.Sugar;
import cn.autolabor.util.collections.Pair;
import cn.autolabor.util.lambda.LambdaFunWithName;
import cn.autolabor.util.lambda.Lambdas;
import cn.autolabor.util.lambda.function.TaskLambdaFun;
import cn.autolabor.util.reflect.TypeNode;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@TaskProperties
public abstract class AbstractTask extends ScheduledPriorityItem {

    private static AtomicLong seq = new AtomicLong(10000L);
    public volatile ReentrantLock runLock = new ReentrantLock();
    public volatile ReentrantLock waitdataLock = new ReentrantLock();

    public volatile ReentrantLock preemptiveWaitLock = new ReentrantLock();
    public volatile Condition preemptiveWaitCondition = preemptiveWaitLock.newCondition();

    public final AtomicBoolean handleFlag = new AtomicBoolean(false);
    // 任务属性
    private final String name;
    private final int priority;
    private final boolean unique;
    private final boolean preemptive;
    private final long sequenceNumber = seq.addAndGet(1L);
    // 任务状态
    public volatile boolean register = false;
    public volatile boolean suspend = false;
    public volatile boolean debug = false;
    public volatile boolean silentInit = false;
    // 准备执行标签
    private volatile Thread runner;
    // 任务执行数据
    protected volatile Queue<ComparableFunctionItem> waitingData = new PriorityBlockingQueue<>();
    private volatile long executeTime;
    private volatile String currentEventName;
    private volatile TaskLambdaFun currentFunction;
    private volatile Object[] params;
    // 子任务
    private ConcurrentHashMap<String, SimpleTask> subTasks = new ConcurrentHashMap<>();
    // 缓存
    private ConcurrentMap<String, TaskMethod> methodBuffer = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Field> parameterBuffer = new ConcurrentHashMap<>();
    // 代理
    private TreeSet<TaskProxy> taskProxies = new TreeSet<>();
    private TreeSet<TaskProxy> desTaskProxies = (TreeSet<TaskProxy>) taskProxies.descendingSet();
    // 统计
    private boolean needTrackBehavior = false;
    private TaskRelationGraph relationGraph = new TaskRelationGraph();

    //    private boolean tmp = false;

    protected AbstractTask(String... name) {
        TaskProperties taskProperties = this.getClass().getAnnotation(TaskProperties.class);
        ArgsParse argsParse = new ArgsParse(name);
        String[] className = this.getClass().getName().split("\\.");
        if (null != taskProperties) {
            this.priority = argsParse.getPriorityOrDefault(taskProperties.priority());
            this.unique = argsParse.getUniqueOrDefault(taskProperties.unique());
            this.preemptive = argsParse.getPreemptiveOrDefault(taskProperties.preemptive());

            String tmpName = Strings.isBlank(taskProperties.name()) ? className[className.length - 1] : taskProperties.name();
            this.name = argsParse.getTaskNameOrDefault(this.unique ? tmpName : String.format("%s_%s", tmpName, Strings.getShortUUID()));
        } else {
            throw Sugar.makeThrow("[%s] No TaskProperties Annotation added.", this.getClass().getName());
        }

        this.silentInit = argsParse.getSilentInitOrDefault(false);
        this.debug = argsParse.getDebugOrDefault(false);

        this.needTrackBehavior = ServerManager.getSystemConfig("TrackBehavior", false);

        this.executeTime = -1L;
        this.currentEventName = "<init>";

        if (ServerManager.me().activationTask(this)) {
            if (needTrackBehavior) {
                if (TaskThreadLocal.getTask() != null) {
                    addGraphInfo("<init>", System.nanoTime());
                }
                TaskThreadLocal.markTask(this);
            }

            if (!silentInit) {
                this.cacheFunction();
                this.cacheParameter();
                this.injectMessageHandle();
            }
        } else {
            throw Sugar.makeThrow("[%s]-(%s) had already activated.", this.getClass().getName(), this.name);
        }
    }

    void run() {
        runLock.lock();
        try {
            if (runner == null && !suspend) {
                runner = Thread.currentThread();
                if (!runner.isInterrupted()) {
                    // 追踪记录开始
                    if (needTrackBehavior) {
                        TaskThreadLocal.markTask(this);
                    }
                    // 任务代理 - 任务执行前
                    for (TaskProxy proxy : taskProxies) {
                        if (proxy.filter(this, currentEventName)) {
                            proxy.before(this, currentEventName, params);
                        }
                    }
                    // 任务执行
                    Object result = currentFunction.invoke(params);
                    // 任务代理 - 任务执行后
                    for (TaskProxy proxy : desTaskProxies) {
                        if (proxy.filter(this, currentEventName)) {
                            proxy.after(this, currentEventName, result, params);
                        }
                    }
                    // 追踪记录终止
                    if (needTrackBehavior) {
                        TaskThreadLocal.unmarkTask(this);
                    }
                } else {
                    suspend = true;
                }
            }
        } finally {
            if (runner == Thread.currentThread()) {
                runner = null;
            }
            runLock.unlock();
        }
    }

    public void onClose() {

    }

    public void cancel(boolean mayInterruptIfRunning) {
        if (mayInterruptIfRunning) {
            try {
                Thread t = runner;
                if (t != null && !t.isInterrupted()) {
                    t.interrupt();
                }
            } finally {
                suspend = true;
            }
        } else {
            suspend = true;
        }

    }

    private void clearExecuteInfo() {
        waitingData.clear();
        executeTime = -1;
        currentEventName = null;
        currentFunction = null;
        params = null;
    }

    boolean changeExecuteTime() {
        waitdataLock.lock();
        try {
            ComparableFunctionItem comparableFunctionItem = new ComparableFunctionItem(this.executeTime, this.currentEventName, this.currentFunction, this.params);
            waitingData.add(comparableFunctionItem);
            return prepareData();
        } finally {
            waitdataLock.unlock();
        }
    }

    boolean prepareData() {
        waitdataLock.lock();
        try {
            ComparableFunctionItem comparableFunctionItem = waitingData.poll();
            if (comparableFunctionItem != null) {
                this.executeTime = comparableFunctionItem.getTime();
                this.currentEventName = comparableFunctionItem.getEventName();
                this.currentFunction = comparableFunctionItem.getMethod();
                this.params = comparableFunctionItem.getParams();
                return true;
            }
            return false;
        } finally {
            waitdataLock.unlock();
        }
    }

    void addWaitingData(Long time, String userDefindName, TaskLambdaFun fun, Object... params) {
        if (needTrackBehavior) {
            addGraphInfo(userDefindName, time);
        }
        ComparableFunctionItem comparableFunctionItem = new ComparableFunctionItem(time, userDefindName, fun, params);
        waitingData.add(comparableFunctionItem);
    }

    void addWaitingData(Long time, String functionName, Object... params) {
        TaskMethod m = methodBuffer.getOrDefault(functionName, null);
        if (m != null) {
            addWaitingData(time, functionName, m.getFun(), params);
        } else {
            System.err.println(String.format("Can't find Function %s in %s", functionName, getTaskName()));
            //TODO: 找不到方法警告
        }
    }

    void addGraphInfo(String functionName, Long time) {
        long currentTime = System.nanoTime();
        Pair<AbstractTask, MessageHandle> info = TaskThreadLocal.get();
        if (info.getKey() != null) {
            relationGraph.add(new TaskEventVertex(info.getKey().getTaskName(), info.getKey().getCurrentEventName()), new TaskEventVertex(this.getTaskName(), functionName), info.getValue() == null ? new TaskCallEdge(Math.round((time - currentTime) / 1e6)) : new MessageCallEdge(info.getValue().getTopic()));
            //            System.err.println(String.format("[%s].[%s] --%s--> [%s].[%s]", info.getKey().getTaskName(), info.getKey().getCurrentEventName(), info.getValue() == null ? String.format("(%d)", Math.round((time - currentTime) / 1e6)) : String.format("|%s|", info.getValue().getTopic()), this.getTaskName(), functionName));
        } else {
            StackTraceElement taskTrace = ServerManager.me().getTaskTrace();
            if (taskTrace != null) {
                relationGraph.add(new ClassFunctionVertex(taskTrace.getClassName(), taskTrace.getMethodName()), new TaskEventVertex(this.getTaskName(), functionName), new FunctionCallEdge());
                //                System.err.println(String.format("{%s}.{%s} --> [%s].[%s]", Strings.lastClassName(taskTrace.getClassName()), taskTrace.getMethodName(), this.getTaskName(), functionName));
            } else {
                relationGraph.add(new UnknowTypeVertex(), new TaskEventVertex(this.getTaskName(), functionName), new FunctionCallEdge());
                //                System.err.println(String.format("未知 --> [%s].[%s]", this.getTaskName(), functionName));
            }
        }
    }

    public TaskMethod getEvent(String event) {
        return methodBuffer.get(event);
    }

    private void cacheFunction() {
        Method[] methods = this.getClass().getMethods();
        for (Method m : methods) {
            TaskFunction t = m.getAnnotation(TaskFunction.class);
            if (t != null) {
                TaskMethod method = new TaskMethod();
                method.setMethodName(m.getName());
                method.setFun(Lambdas.toFun(MethodHandles.lookup(), m, this));
                method.setReturnType(new TypeNode(m.getGenericReturnType()));
                Type[] types = m.getGenericParameterTypes();
                TypeNode[] params = new TypeNode[types.length];
                for (int i = 0; i < params.length; i++) {
                    params[i] = new TypeNode(types[i]);
                }
                method.setParamsType(params);
                methodBuffer.put(Strings.isBlank(t.name()) ? m.getName() : t.name(), method);
            }
        }
    }

    private void cacheParameter() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            TaskParameter p = f.getAnnotation(TaskParameter.class);
            if (p != null) {
                parameterBuffer.put(p.name(), f);
                if (ServerManager.me().checkConfig(this, p.name())) {
                    updateParameter(p.name(), ServerManager.me().getConfig(this, p.name()));
                } else {
                    ServerManager.me().setConfig(this, p.name(), Strings.fromString(p.value(), f.getType()));
                }
            }
        }
    }

    private void injectMessageHandle() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            InjectMessage p = f.getAnnotation(InjectMessage.class);
            if (p != null && Sugar.checkInherit(f.getType(), MessageHandle.class)) {
                Type type = f.getGenericType();
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    Type[] types = pType.getActualTypeArguments();
                    if (types.length == 1) {
                        try {
                            f.set(this, ServerManager.me().getOrCreateMessageHandle(p.topic(), new TypeNode(types[0])));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw Sugar.makeThrow("The generics of MessageHandle %s not correct", f.getName());
                    }
                } else {
                    throw Sugar.makeThrow("MessageHandle %s need to specify generics", f.getName());
                }

            }
        }
    }

    public void updateParameter(String paramName, Object value) {
        Field f = this.parameterBuffer.getOrDefault(paramName, null);
        if (f != null) {
            try {
                f.set(this, value);
                TaskParameter p = f.getAnnotation(TaskParameter.class);
                if (p != null && Strings.isNotBlank(p.event())) {
                    ServerManager.me().run(this, p.event(), value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Object syncRun(String eventName, Object... params) {
        TaskMethod event = this.getEvent(eventName);
        if (event != null) {
            final ReentrantLock lock = this.runLock;
            lock.lock();
            try {
                return event.getFun().invoke(params);
            } finally {
                lock.unlock();
            }
        } else {
            throw Sugar.makeThrow("No %s Function", eventName);
        }
    }

    public Object syncRun(LambdaFunWithName lambdaFunWithName, Object... params) {
        final ReentrantLock lock = this.runLock;
        lock.lock();
        try {
            return lambdaFunWithName.getFun().invoke(params);
        } finally {
            lock.unlock();
        }
    }

    public void asyncRun(String eventName, Object... params) {
        ServerManager.me().run(this, eventName, params);
    }

    public void asyncRunLater(LambdaFunWithName lambdaFunWithName, Long later, Object... params) {
        ServerManager.me().run(this, later, lambdaFunWithName, params);
    }

    public void asyncRunLater(String eventName, Long later, Object... params) {
        ServerManager.me().run(this, later, eventName, params);
    }

    public void asyncRunDelay(LambdaFunWithName lambdaFunWithName, Long delay, Object... params) {
        ServerManager.me().delayRun(this, delay, lambdaFunWithName, params);
    }

    public void asyncRunDelay(String eventName, Long delay, Object... params) {
        ServerManager.me().delayRun(this, delay, eventName, params);
    }

    public SimpleTask applySubTask(String subTaskName, int priority, boolean preemptive) {
        synchronized (subTasks) {
            SimpleTask task = subTasks.get(subTaskName);
            if (task == null) {
                task = new SimpleTask(this.getTaskName(), subTaskName, priority, preemptive);
                subTasks.put(subTaskName, task);
                return task;
            } else {
                if (task.getPriority() == priority && task.isPreemptive() == preemptive) {
                    return task;
                } else {
                    throw Sugar.makeThrow("Subtask %s does not match the given configuration parameters", subTaskName);
                }
            }
        }
    }

    public void asyncRunWithSubTask(String simpleTaskName, String eventName, Object... params) {
        asyncRunWithSubTask(simpleTaskName, 5, false, eventName, params);
    }

    public void asyncRunWithSubTask(String simpleTaskName, LambdaFunWithName lambdaFunWithName, Object... params) {
        asyncRunWithSubTask(simpleTaskName, 5, false, lambdaFunWithName, params);
    }

    public void asyncRunWithSubTask(String simpleTaskName, int priority, boolean preemptive, String eventName, Object... params) {
        asyncRunWithSubTask(applySubTask(simpleTaskName, priority, preemptive), eventName, params);
    }

    public void asyncRunWithSubTask(String simpleTaskName, int priority, boolean preemptive, LambdaFunWithName lambdaFunWithName, Object... params) {
        asyncRunWithSubTask(applySubTask(simpleTaskName, priority, preemptive), lambdaFunWithName, params);
    }

    public void asyncRunWithSubTask(SimpleTask simpleTask, String eventName, Object... params) {
        TaskMethod m = methodBuffer.getOrDefault(eventName, null);
        if (m != null) {
            asyncRunWithSubTask(simpleTask, new LambdaFunWithName(eventName, m.getFun()), params);
        } else {
            System.err.println(String.format("Can't find Function %s in %s", eventName, getTaskName()));
        }
    }

    public void asyncRunWithSubTask(SimpleTask simpleTask, LambdaFunWithName lambdaFunWithName, Object... params) {
        ServerManager.me().run(simpleTask, lambdaFunWithName, params);
    }

    public void addProxy(TaskProxy taskProxy) {
        taskProxies.add(taskProxy);
    }

    public void removeProxy(Class taskProxyClass) {
        if (taskProxies.size() <= 0) {
            return;
        }

        for (TaskProxy proxy : taskProxies) {
            if (Sugar.checkInherit(proxy.getClass(), taskProxyClass)) {
                taskProxies.remove(proxy);
            }
        }
    }

    public long getTime() {
        return this.executeTime;
    }

    public void setTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public long getSequence() {
        return this.sequenceNumber;
    }

    public int getPriority() {
        return priority;
    }

    public String getTaskName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isPreemptive() {
        return preemptive;
    }

    public boolean isStandard() {
        return !preemptive;
    }

    public Thread getRunner() {
        return runner;
    }

    public String getCurrentEventName() {
        return currentEventName;
    }

    public List<String> getAllEventsName() {
        return new ArrayList<>(methodBuffer.keySet());
    }

    public boolean isNeedTrackBehavior() {
        return needTrackBehavior;
    }

    public TaskRelationGraph getRelationGraph() {
        return relationGraph;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AbstractTask task = (AbstractTask) o;

        if (sequenceNumber != task.sequenceNumber)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (sequenceNumber ^ (sequenceNumber >>> 32));
    }

    @Override
    public String toString() {
        return "AbstractTask{" + "name='" + name + '\'' + ", priority=" + priority + ", unique=" + unique + ", " + "preemptive=" + preemptive + ", sequenceNumber=" + sequenceNumber + '}';
    }
}
