package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun04<Q, W, E, R> extends TaskLambdaFunWithoutReturn {

    void run(Q arg0, W arg1, E arg2, R arg3);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN04;
    }
}
