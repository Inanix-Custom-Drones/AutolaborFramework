package cn.autolabor.util.lambda.function;

@FunctionalInterface
public interface TaskLambdaFun05<Q, W, E, R, T> extends TaskLambdaFunWithoutReturn {

    void run(Q arg0, W arg1, E arg2, R arg3, T arg4);

    default TaskLambdaFunctionCode code() {
        return TaskLambdaFunctionCode.FUN05;
    }
}
