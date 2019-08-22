package cn.autolabor.core.annotation;

import cn.autolabor.core.server.message.MessageSourceType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
@Documented
public @interface SubscribeMessage {

    String topic();

    // 空代表所有
    MessageSourceType[] source() default {};

}
