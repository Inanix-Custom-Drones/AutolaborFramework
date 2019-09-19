package cn.autolabor.util.lambda;

import cn.autolabor.util.lambda.function.TaskLambdaFun;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LambdaFunWithName that = (LambdaFunWithName) o;

        if (!Objects.equals(name, that.name))
            return false;
        return Objects.equals(fun, that.fun);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fun != null ? fun.hashCode() : 0);
        return result;
    }
}
