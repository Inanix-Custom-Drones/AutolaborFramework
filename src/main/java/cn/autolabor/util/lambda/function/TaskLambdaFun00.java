package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun00 extends TaskLambdaFunWithoutReturn {

    void run();

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN00;
    }
}
