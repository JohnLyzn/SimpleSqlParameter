package com.fy.sqlparam.param;

import com.fy.sqlparam.map.ISqlMapContext;
import com.fy.sqlparam.map.ISqlMapper;

/**
 * SQL搜索参数
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlParameter {
	
	/**
	 * 添加查询条件, 如果这不是第一个查询条件则默认使用AND关系连接
	 * 
	 * @param query 添加的查询条件, 不能为null
	 * @return 对应的查询实例, 主要用来
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQueryIndex query(ISqlQuery query);
	
	/**
	 * 标记某个属性进行排序
	 * 
	 * @param propertyName 属性名称, 不能为null
	 * @param isAsc 是否是正序排序, 为<tt>false</tt>则为逆序排序
	 * @return 对应的查询实例
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQueryIndex markOrderBy(String propertyName, boolean isAsc);
	
	/**
	 * 设置查询分页
	 * 
	 * @param page 第几页, 不能小于1, 小于的情况下按1处理
	 * @param count 一页包含的数量, 不能小于0, 小于的情况下按10处理
	 * @param offset 起点偏移数, 不能小于0, 小于的情况下按0处理
	 * @return 对应的查询实例
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQueryIndex setPagination(int page, int count, int offset);
	
	/**
	 * 删除查询
	 * 
	 * @param queryIndex 删除的查询条件索引, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.1
	 */
	void deleteQuery(ISqlQueryIndex queryIndex);
	
	/**
	 * 删除所有条件查询
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void deleteConditions();
	
	/**
	 * 删除所有排序查询
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void deleteSorts();
	
	/**
	 * 删除分页的查询
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void deletePagination();
	
	/**
	 * 清除所有查询条件, 包含排序和分页
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void clear();
	
	/**
	 * 生成映射上下文实例
	 * 
	 * @param paramContext 指定生成的映射上下文所属的搜索参数上下文, 不能为null
	 * @param sqlMapper 指定生成的映射上下文所使用的映射处理器, 不能为null
	 * @return 映射上下文实例
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlMapContext generateMapContext(ISqlParameterContext paramContext, ISqlMapper sqlMapper);
}
