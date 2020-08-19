package com.fy.sqlparam.map.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段注解
 * <br/> 会被实例化为映射元信息
 * 
 * @author linjie
 * @since 1.0.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapMeta {

	/**
	 * 字段名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String name();
	
	/**
	 * 字段对应的数据库字段名称, 包括表别名
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String value();
	
	/**
	 * 依赖的关联表的名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String[] dependencyNames() default {};
	
	/**
	 * 当该字段被查询时, 同时触发添加的查询
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Query[] triggleQueries() default {};
}
