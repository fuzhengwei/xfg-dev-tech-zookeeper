package cn.bugstack.xfg.dev.tech.types;

import java.lang.annotation.*;

/**
 * 注解，动态配置中心
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface DCCValue {

    String value() default "";

}
