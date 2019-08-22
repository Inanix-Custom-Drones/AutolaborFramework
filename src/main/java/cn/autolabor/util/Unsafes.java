package cn.autolabor.util;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Unsafes {

    static Object unsafe;
    static Method allocateInstance;

    static {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = f.get(null);
            allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Object allocateInstance(Class clazz) throws InvocationTargetException, IllegalAccessException {
        return allocateInstance.invoke(unsafe, clazz);
    }

}
