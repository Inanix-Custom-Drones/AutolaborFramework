package cn.autolabor.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.FIELD})
@Documented
@Inherited
public @interface TaskProperties {

    String name() default "";

    int priority() default 5;

    boolean unique() default true;

    boolean preemptive() default false;
}