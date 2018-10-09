package com.fy.sqlparam.param;

import java.util.Collection;

/**
 * SQL查询组
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlQueryGroup extends ISqlQuery {

	/**
	 * 添加查询到查询组中
	 * 
	 * @return 是否是添加成功, 是则为true, 否则为false
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	boolean addQuery(ISqlQuery query);
	
	/**
	 * 移除查询
	 * 
	 * @return 是否是移除成功, 是则为true, 否则为false
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	boolean removeQuery(ISqlQuery query);
	
	/**
	 * 获取对应的查询实例列表
	 * 
	 * @return 对应的查询实例
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Collection<ISqlQuery> getQueries();
}
