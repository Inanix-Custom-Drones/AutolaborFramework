package cn.autolabor.util.lambda;

import cn.autolabor.util.Sugar;
import cn.autolabor.util.lambda.function.*;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Lambdas {


    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<Class<?>, Class<?>>() {
        {
            put(boolean.class, Boolean.class);
            put(byte.class, Byte.class);
            put(char.class, Character.class);
            put(double.class, Double.class);
            put(float.class, Float.class);
            put(int.class, Integer.class);
            put(long.class, Long.class);
            put(short.class, Short.class);
            put(void.class, Void.class);
        }
    };

    public static MethodHandle toMethodhandle(MethodHandles.Lookup lookup, Method method) {
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun00 fun) {
        try {
            Method method = fun.getClass().getMethod("run");
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <Q> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun01<Q> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <Q, W> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun02<Q, W> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <Q, W, E> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun03<Q, W, E> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <Q, W, E, R> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun04<Q, W, E, R> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <Q, W, E, R, T> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun05<Q, W, E, R, T> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <Q, W, E, R, T, Y> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun06<Q, W, E, R, T, Y> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <A> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun10<A> fun) {
        try {
            Method method = fun.getClass().getMethod("run");
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <A, Q> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun11<A, Q> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <A, Q, W> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun12<A, Q, W> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <A, Q, W, E> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun13<A, Q, W, E> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <A, Q, W, E, R> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun14<A, Q, W, E, R> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <A, Q, W, E, R, T> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun15<A, Q, W, E, R, T> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <A, Q, W, E, R, T, Y> MethodHandle toMethodhandle(MethodHandles.Lookup lookup, TaskLambdaFun16<A, Q, W, E, R, T, Y> fun) {
        try {
            Method method = fun.getClass().getMethod("run", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class);
            return lookup.unreflect(method).bindTo(fun);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LambdaFunWithName toFun(String name, TaskLambdaFun00 fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <Q> LambdaFunWithName toFun(String name, TaskLambdaFun01<Q> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <Q, W> LambdaFunWithName toFun(String name, TaskLambdaFun02<Q, W> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <Q, W, E> LambdaFunWithName toFun(String name, TaskLambdaFun03<Q, W, E> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <Q, W, E, R> LambdaFunWithName toFun(String name, TaskLambdaFun04<Q, W, E, R> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <Q, W, E, R, T> LambdaFunWithName toFun(String name, TaskLambdaFun05<Q, W, E, R, T> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <Q, W, E, R, T, Y> LambdaFunWithName toFun(String name, TaskLambdaFun06<Q, W, E, R, T, Y> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <A> LambdaFunWithName toFun(String name, TaskLambdaFun10<A> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <A, Q> LambdaFunWithName toFun(String name, TaskLambdaFun11<A, Q> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <A, Q, W> LambdaFunWithName toFun(String name, TaskLambdaFun12<A, Q, W> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <A, Q, W, E> LambdaFunWithName toFun(String name, TaskLambdaFun13<A, Q, W, E> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <A, Q, W, E, R> LambdaFunWithName toFun(String name, TaskLambdaFun14<A, Q, W, E, R> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <A, Q, W, E, R, T> LambdaFunWithName toFun(String name, TaskLambdaFun15<A, Q, W, E, R, T> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static <A, Q, W, E, R, T, Y> LambdaFunWithName toFun(String name, TaskLambdaFun16<A, Q, W, E, R, T, Y> fun) {
        return new LambdaFunWithName(name, fun);
    }

    public static TaskLambdaFun toFun(MethodHandles.Lookup lookup, MethodHandle mh) {
        if (mh != null) {
            MethodType mt = mh.type();
            try {
                CallSite site = LambdaMetafactory.metafactory(lookup, "run", MethodType.methodType(choiceFun(mt)), choiceGeneralType(mt, false), mh, mt);
                return (TaskLambdaFun) site.getTarget().invoke();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return null;
    }

    public static TaskLambdaFun toFun(MethodHandles.Lookup lookup, MethodHandle mh, Object instance) {
        if (mh != null) {
            MethodType mt = mh.type();
            try {
                MethodType generalType = choiceGeneralType(mt, true);
                MethodType methodType = MethodType.methodType(mt.returnType(), wrapClass(mt.parameterArray(), true));
                CallSite site = LambdaMetafactory.metafactory(lookup, "run", MethodType.methodType(choiceFun(generalType), instance.getClass()), generalType, mh, methodType);
                return (TaskLambdaFun) site.getTarget().bindTo(instance).invoke();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return null;
    }

    public static TaskLambdaFun toFun(MethodHandles.Lookup lookup, Method m) {
        return Lambdas.toFun(lookup, Lambdas.toMethodhandle(lookup, m));
    }

    public static TaskLambdaFun toFun(MethodHandles.Lookup lookup, Method m, Object instance) {
        return Lambdas.toFun(lookup, Lambdas.toMethodhandle(lookup, m), instance);
    }

    private static Class[] wrapClass(Class[] paramClass, boolean forBind) {
        Class[] wrap = null;
        int count = 0;
        if (forBind) {
            wrap = new Class[paramClass.length - 1];
            for (int i = 1; i < paramClass.length; i++) {
                Class c = paramClass[i];
                wrap[count++] = c.isPrimitive() ? PRIMITIVES_TO_WRAPPERS.get(c) : c;
            }
        } else {
            wrap = new Class[paramClass.length];
            for (int i = 0; i < paramClass.length; i++) {
                Class c = paramClass[i];
                wrap[count++] = c.isPrimitive() ? PRIMITIVES_TO_WRAPPERS.get(c) : c;
            }
        }
        return wrap;
    }

    private static MethodType choiceGeneralType(MethodType methodType, boolean forBind) {
        final Class returnType = methodType.returnType().equals(void.class) ? void.class : Object.class;
        final Class[] paramsType = new Class[forBind ? methodType.parameterCount() - 1 : methodType.parameterCount()];
        Arrays.fill(paramsType, Object.class);
        return MethodType.methodType(returnType, paramsType);
    }

    private static Class choiceFun(MethodType methodType) {
        if (methodType.returnType().equals(void.class)) {
            switch (methodType.parameterCount()) {
                case 0:
                    return TaskLambdaFun00.class;
                case 1:
                    return TaskLambdaFun01.class;
                case 2:
                    return TaskLambdaFun02.class;
                case 3:
                    return TaskLambdaFun03.class;
                case 4:
                    return TaskLambdaFun04.class;
                case 5:
                    return TaskLambdaFun05.class;
                case 6:
                    return TaskLambdaFun06.class;
                default:
                    throw Sugar.makeThrow("Not support more than 6 parameter methods");
            }
        } else {
            switch (methodType.parameterCount()) {
                case 0:
                    return TaskLambdaFun10.class;
                case 1:
                    return TaskLambdaFun11.class;
                case 2:
                    return TaskLambdaFun12.class;
                case 3:
                    return TaskLambdaFun13.class;
                case 4:
                    return TaskLambdaFun14.class;
                case 5:
                    return TaskLambdaFun15.class;
                case 6:
                    return TaskLambdaFun16.class;
                default:
                    throw Sugar.makeThrow("Not support more than 6 parameter methods");
            }
        }
    }

}
