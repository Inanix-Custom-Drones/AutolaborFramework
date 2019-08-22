package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun14<A, Q, W, E, R> extends TaskLambdaFunWithReturn {

    A run(Q arg0, W arg1, E arg2, R arg3);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN14;
    }
}
