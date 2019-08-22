package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun02<Q, W> extends TaskLambdaFunWithoutReturn {

    void run(Q arg0, W arg1);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN02;
    }
}
