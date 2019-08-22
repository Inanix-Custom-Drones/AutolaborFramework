package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun10<A> extends TaskLambdaFunWithReturn {

    A run();

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN10;
    }
}
