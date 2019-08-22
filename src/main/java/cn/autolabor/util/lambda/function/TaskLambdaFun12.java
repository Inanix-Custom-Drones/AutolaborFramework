package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun12<A, Q, W> extends TaskLambdaFunWithReturn {

    A run(Q arg0, W arg1);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN12;
    }
}
