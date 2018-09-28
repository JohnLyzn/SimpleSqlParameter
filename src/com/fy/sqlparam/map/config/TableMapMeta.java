package com.fy.sqlparam.map.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 关联表注解
 * <br/> 会被实例化为SQL成员
 * 
 * @author linjie
 * @since 1.0.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableMapMeta {

	/**
	 * 关联表的名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String name();
	
	/**
	 * 完整的表关联语句, 尽量只包含一张关联表, 其它用依赖关系来拼接
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String value();
	
	/**
	 * 依赖的其它关联表的名称
	 * 
	 * @return 依赖的其它关联表的名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String[] dependencyNames() default {};
}
