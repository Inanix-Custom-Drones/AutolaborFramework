package cn.autolabor.core.server.executor;

import cn.autolabor.core.server.ServerManager;
import cn.autolabor.core.server.message.MessageHandle;
import cn.autolabor.util.collections.Pair;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class TaskThreadLocal {

    private static ConcurrentHashMap<String, Stack<AbstractTask>> taskMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, MessageHandle> topicMap = new ConcurrentHashMap<>();

    public static void markTask(AbstractTask task) {
        String threadKey = Thread.currentThread().getName();
        if (taskMap.containsKey(threadKey)) {
            taskMap.get(threadKey).push(task);
        } else {
            taskMap.put(threadKey, new Stack<AbstractTask>() {{
                push(task);
            }});
        }
    }

    public static void markTopic(MessageHandle handle) {
        String threadKey = Thread.currentThread().getName();
        topicMap.put(threadKey, handle);
    }

    public static void unmarkTask(AbstractTask task) {
        String threadKey = Thread.currentThread().getName();

        if (taskMap.containsKey(threadKey)) {
            Stack<AbstractTask> tasksStack = taskMap.get(threadKey);
            if (task.equals(tasksStack.peek())) {
                tasksStack.pop();
            } else {
                taskMap.remove(threadKey);
            }
        }
    }

    public static void unmarkTopic() {
        String threadKey = Thread.currentThread().getName();
        topicMap.remove(threadKey);
    }

    public static AbstractTask getTask() {
        String threadKey = Thread.currentThread().getName();
        StackTraceElement taskTrace = ServerManager.me().getTaskTrace();
        AbstractTask currentTask = null;
        if (taskMap.containsKey(threadKey) && taskTrace != null) {
            currentTask = find(taskMap.get(threadKey), taskTrace.getClassName());
            if (currentTask == null && !taskMap.get(threadKey).empty()) {
                currentTask = taskMap.get(threadKey).peek();
            }
        }
        return currentTask;
    }

    public static MessageHandle getTopic() {
        String threadKey = Thread.currentThread().getName();
        return topicMap.getOrDefault(threadKey, null);
    }

    public static Pair<AbstractTask, MessageHandle> get() {
        String threadKey = Thread.currentThread().getName();
        StackTraceElement taskTrace = ServerManager.me().getTaskTrace();
        AbstractTask currentTask = null;
        if (taskMap.containsKey(threadKey) && taskTrace != null) {
            currentTask = find(taskMap.get(threadKey), taskTrace.getClassName());
            if (currentTask == null && !taskMap.get(threadKey).empty()) {
                currentTask = taskMap.get(threadKey).peek();
            }
        }

        MessageHandle currentTopic = topicMap.getOrDefault(threadKey, null);

        return new Pair<>(currentTask, currentTopic);
    }

    private static AbstractTask find(Stack<AbstractTask> stack, String taskClassName) {
        for (AbstractTask aStack : stack) {
            if (aStack.getClass().getName().equals(taskClassName)) {
                return aStack;
            }
        }
        return null;
    }

}
