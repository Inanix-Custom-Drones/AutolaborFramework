package cn.autolabor.util.reflect;

import cn.autolabor.message.navigation.Msg2DTwist;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TypeNode {
    private boolean isArray;
    private boolean isParameterized;
    private Type type;
    private TypeNode[] arguments;

    public TypeNode(Type type) {
        this.type = type;
        if (type instanceof ParameterizedType) {
            this.isParameterized = true;
            this.isArray = false;
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (null != types && types.length > 0) {
                this.arguments = new TypeNode[types.length];
                for (int i = 0; i < this.arguments.length; i++) {
                    this.arguments[i] = new TypeNode(types[i]);
                }
            }
        } else if (type instanceof GenericArrayType) {
            this.isArray = true;
            this.isParameterized = false;
            this.arguments = new TypeNode[]{new TypeNode(((GenericArrayType) type).getGenericComponentType())};
        } else {
            Class rawType = (Class) type;
            if (rawType.isArray()) {
                this.isArray = true;
                this.arguments = new TypeNode[]{new TypeNode(rawType.getComponentType())};
            } else {
                this.isArray = false;
            }
            this.isParameterized = false;

        }
    }

    public static void main(String[] args) {
        TypeNode node = new TypeFetch<Map<int[][], List<Msg2DTwist>[]>>() {
        }.getTypeNode();
        System.out.println(node);
    }

    public Type getType() {
        return type;
    }

    public Class getRawType() {
        return Reflects.getRawType(type);
    }

    public TypeNode[] getArguments() {
        return arguments;
    }

    public TypeNode getArguments(int... index) {
        TypeNode result = null;
        for (int anIndex : index) {
            result = getArguments(anIndex);
            if (result == null) {
                return null;
            }
        }
        return result;
    }

    public TypeNode getArguments(int index) {
        if (index >= 0 && arguments != null && index < arguments.length) {
            return arguments[index];
        } else {
            return null;
        }
    }

    public boolean isPrimitive() {
        return (type instanceof Class && ((Class) type).isPrimitive());
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isParameterized() {
        return isParameterized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TypeNode typeNode = (TypeNode) o;

        return type.equals(typeNode.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "TypeNode{" + "isParameterized=" + isParameterized + ", isArray=" + isArray + ", type=" + type + ", typeClass=" + type.getClass() + ", arguments=" + Arrays.toString(arguments) + '}';
    }
}