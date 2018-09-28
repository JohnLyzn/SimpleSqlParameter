package com.fy.sqlparam.param;

/**
 * SQL查询索引
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlQueryIndex {

	/**
	 * 是否是查询组
	 * 
	 * @return 是否是查询组, 是则为true, 否则为false
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	boolean isGroup();
	
	/**
	 * 获取查询的字段民名称
	 * 
	 * @return 查询的字段民名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String getQueryName();
	
	/**
	 * 获取对应的查询实例
	 * 
	 * @return 对应的查询实例
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery getQuery();
}
