package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun13<A, Q, W, E> extends TaskLambdaFunWithReturn {

    A run(Q arg0, W arg1, E arg2);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN13;
    }
}
