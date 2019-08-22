package cn.autolabor.core.server.executor;

import cn.autolabor.util.lambda.function.TaskLambdaFun;
import cn.autolabor.util.reflect.TypeNode;

import java.util.Arrays;

public class TaskMethod {

    private String methodName;
    private TaskLambdaFun fun;
    private TypeNode returnType;
    private TypeNode[] paramsType;

    public TaskMethod() {
    }

    public TaskMethod(String methodName, TaskLambdaFun fun, TypeNode returnType, TypeNode[] paramsType) {
        this.methodName = methodName;
        this.fun = fun;
        this.returnType = returnType;
        this.paramsType = paramsType;
    }

    public TaskLambdaFun getFun() {
        return fun;
    }

    public void setFun(TaskLambdaFun fun) {
        this.fun = fun;
    }

    public TypeNode getReturnType() {
        return returnType;
    }

    public void setReturnType(TypeNode returnType) {
        this.returnType = returnType;
    }

    public TypeNode[] getParamsType() {
        return paramsType;
    }

    public void setParamsType(TypeNode[] paramsType) {
        this.paramsType = paramsType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "TaskMethod{" + "methodName='" + methodName + '\'' + ", fun=" + fun + ", returnType=" + returnType + ", paramsType=" + Arrays.toString(paramsType) + '}';
    }
}
