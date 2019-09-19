package cn.autolabor.util.reflect;

import cn.autolabor.module.networkhub.UDPMulticastBroadcaster;
import cn.autolabor.util.Sugar;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Reflects {

    public static boolean isString(Type type) {
        return isClass(type) && type.equals(String.class);
    }

    public static boolean isBoolean(Class type) {
        return type.equals(boolean.class) || type.equals(Boolean.class);
    }

    public static boolean isBoolean(Type type) {
        return (isClass(type)) && isBoolean((Class) type);
    }

    public static boolean isByte(Class type) {
        return (type.equals(byte.class) || type.equals(Byte.class));
    }

    public static boolean isByte(Type type) {
        return (isClass(type)) && isByte((Class) type);
    }

    public static boolean isChar(Class type) {
        return (type.equals(char.class) || type.equals(Character.class));
    }

    public static boolean isChar(Type type) {
        return (isClass(type)) && isChar((Class) type);
    }

    public static boolean isShort(Class type) {
        return (type.equals(short.class) || type.equals(Short.class));
    }

    public static boolean isShort(Type type) {
        return (isClass(type)) && isShort((Class) type);
    }

    public static boolean isInt(Class type) {
        return (type.equals(int.class) || type.equals(Integer.class));
    }

    public static boolean isInt(Type type) {
        return (isClass(type)) && isInt((Class) type);
    }

    public static boolean isLong(Class type) {
        return (type.equals(long.class) || type.equals(Long.class));
    }

    public static boolean isLong(Type type) {
        return (isClass(type)) && isLong((Class) type);
    }

    public static boolean isFloat(Class type) {
        return (type.equals(float.class) || type.equals(Float.class));
    }

    public static boolean isFloat(Type type) {
        return (isClass(type)) && isFloat((Class) type);
    }

    public static boolean isDouble(Class type) {
        return (type.equals(double.class) || type.equals(Double.class));
    }

    public static boolean isDouble(Type type) {
        return (isClass(type)) && isDouble((Class) type);
    }

    public static boolean isClass(Type type) {
        return type instanceof Class;
    }

    public static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }

    public static boolean isGenericArrayType(Type type) {
        return type instanceof GenericArrayType;
    }

    public static boolean isArray(Type type) {
        return (isClass(type) && ((Class) type).isArray()) || isGenericArrayType(type);
    }

    public static Class getRawType(Type type) {
        if (isClass(type)) {
            return (Class) type;
        } else if (isParameterizedType(type)) {
            return (Class) ((ParameterizedType) type).getRawType();
        } else if (isGenericArrayType(type)) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return Array.newInstance(getRawType(arrayType.getGenericComponentType()), 0).getClass();
        } else {
            return null;
        }
    }

    public static boolean isList(Type type) {
        Class rawType = getRawType(type);
        return rawType != null && Sugar.checkInherit(rawType, List.class);
    }

    public static boolean isMap(Type type) {
        Class rawType = getRawType(type);
        return rawType != null && Sugar.checkInherit(rawType, Map.class);
    }

    public static boolean checkMethodParameter(Method m, Class... classes) {
        if (m.getParameterCount() != classes.length) {
            return false;
        }

        Class[] methodParameterType = m.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            if (!Sugar.checkInherit(classes[i], methodParameterType[i])) {
                return false;
            }
        }
        return true;
    }

    public static List<Method> getAllMethods(Class<?> cls) {
        List<Method> methods = new ArrayList<>();
        if (null != cls) {
            Class<?> currentClass = cls;
            while (null != currentClass) {
                Method[] declaredMethods = currentClass.getDeclaredMethods();
                methods.addAll(Arrays.asList(declaredMethods));
                currentClass = currentClass.getSuperclass();
            }
        }
        return methods;
    }

    public static void main(String[] args) {
        getAllMethods(UDPMulticastBroadcaster.class).forEach(System.out::println);
    }

}
