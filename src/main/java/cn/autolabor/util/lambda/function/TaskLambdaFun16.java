package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun16<A, Q, W, E, R, T, Y> extends TaskLambdaFunWithReturn {

    A run(Q arg0, W arg1, E arg2, R arg3, T arg4, Y arg5);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN16;
    }
}
