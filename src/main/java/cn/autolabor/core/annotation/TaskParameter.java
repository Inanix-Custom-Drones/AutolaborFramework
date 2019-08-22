package cn.autolabor.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
@Documented
@Inherited
public @interface TaskParameter {

    String name();

    String value();

    String event() default "";

}
