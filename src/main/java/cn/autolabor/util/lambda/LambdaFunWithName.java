package cn.autolabor.util.lambda;

import cn.autolabor.util.lambda.function.TaskLambdaFun;

public class LambdaFunWithName {
    private String name;
    private TaskLambdaFun fun;

    public LambdaFunWithName(String name, TaskLambdaFun fun) {
        this.name = name;
        this.fun = fun;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskLambdaFun getFun() {
        return fun;
    }

    public void setFun(TaskLambdaFun fun) {
        this.fun = fun;
    }
}
