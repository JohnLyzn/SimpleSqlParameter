package com.fy.sqlparam.param;

import java.util.Collection;

/**
 * 查询方法, 提供快速生成查询实例的接口
 * 
 * @author linjie
 * @since 1.0.1
 */
public interface ISqlQueryMethod {

	/**
	 * 查询属性等于某个值
	 * 
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery eq(Object target);

	/**
	 * 	查询属性不等于某个值
	 * 	
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery notEq(Object target);

	/**
	 * 查询属性在某个集合中
	 * 
	 * @param targets 目标集合, 不能为null或空, 集合中的null会被去掉
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery in(Collection<?> targets);

	/**
	 * 查询属性不在某个集合中
	 * 
	 * @param targets 目标集合, 不能为null或空, 集合中的null会被去掉
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery notIn(Collection<?> targets);

	/**
	 * 查询属性模糊匹配某个值
	 * 
	 * @param target 模糊匹配指定的字符串, 格式是中like查询的格式
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery like(String target);

	/**
	 * 查询属性在某个范围内
	 * 
	 * @param from 范围起点, 不能为null
	 * @param to 范围终点, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery between(Object from, Object to);

	/**
	 * 查询属性小于某个值
	 * 
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery lessThan(Object target);

	/**
	 * 查询属性不小于(大于等于)某个值
	 * 
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery notLessThan(Object target);

	/**
	 * 查询属性大于某个值
	 * 
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery greaterThan(Object target);

	/**
	 * 查询属性不大于(小于等于)某个值
	 * 
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery notGreaterThan(Object target);

	/**
	 * 查询属性的值为NULL
	 * 
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #addQueryByAnd(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery isNull();

	/**
	 * 查询属性的值不为NULL
	 * 
	 * @param target 目标值, 不能为null
	 * @return 查询实例, 通过{@link ISqlParameter #query(String, ISqlQuery)}
	 * 	或{@link ISqlParameter #addQueryByOr(String, ISqlQuery)}加入到搜索参数中使用
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQuery isNotNull();
}
