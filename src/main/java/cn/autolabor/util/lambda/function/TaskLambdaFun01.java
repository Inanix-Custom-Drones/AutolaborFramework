package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun01<Q> extends TaskLambdaFunWithoutReturn {

    void run(Q arg0);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN01;
    }
}
