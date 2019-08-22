package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun06<Q, W, E, R, T, Y> extends TaskLambdaFunWithoutReturn {

    void run(Q arg0, W arg1, E arg2, R arg3, T arg4, Y arg5);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN06;
    }
}
