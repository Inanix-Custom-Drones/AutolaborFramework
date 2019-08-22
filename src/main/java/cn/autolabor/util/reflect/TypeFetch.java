package cn.autolabor.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeFetch<T> {

    public TypeNode getTypeNode() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return new TypeNode(type);
    }

}
