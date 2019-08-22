package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun11<A, Q> extends TaskLambdaFunWithReturn {

    A run(Q arg0);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN11;
    }
}
