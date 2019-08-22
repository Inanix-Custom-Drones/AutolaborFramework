package cn.autolabor.core.server.statistics;

import cn.autolabor.util.Strings;
import cn.autolabor.util.collections.graph.Vertex;

public class ClassFunctionVertex extends Vertex {

    private String className;
    private String functionName;

    public ClassFunctionVertex(String className, String functionName) {
        super(String.format("%s.%s", Strings.lastClassName(className), functionName));
        this.className = className;
        this.functionName = functionName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        ClassFunctionVertex that = (ClassFunctionVertex) o;

        if (!className.equals(that.className))
            return false;
        return functionName.equals(that.functionName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + functionName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClassFunctionVertex{" + "className='" + className + '\'' + ", functionName='" + functionName + '\'' + '}';
    }
}
