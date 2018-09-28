package com.fy.sqlparam.map.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询条件注解
 * <br/> 会被实例化为SQL成员
 * 
 * @author linjie
 * @since 1.0.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

	/**
	 * 完整的查询条件语句, 不包含逻辑关系
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String value();
	
	/**
	 * 查询条件连接逻辑关系是否为AND, 默认为true
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	boolean isAnd() default true;
	
	/**
	 * 查询条件依赖的关联表的名称, 可以没有
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String[] denpendencyNames() default {};
}
