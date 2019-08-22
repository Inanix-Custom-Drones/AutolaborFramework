package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun15<A, Q, W, E, R, T> extends TaskLambdaFunWithReturn {

    A run(Q arg0, W arg1, E arg2, R arg3, T arg4);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN15;
    }
}
