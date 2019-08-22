package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun03<Q, W, E> extends TaskLambdaFunWithoutReturn {

    void run(Q arg0, W arg1, E arg2);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN03;
    }
}
