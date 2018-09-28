package com.fy.sqlparam.param;


/**
 * SQL查询
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlQuery {

	/**
	 * 以AND连接关系添加下一个查询条件
	 * <br/> 如果在其中添加了多个条件, 则生成成组的查询, 查询条件外部被'()'包围.
	 * 
	 * @param query 查询条件, 不能为null
	 * @return 指向下一个查询条件的实例, 即传入的查询条件实例, 方便构建链式调用
	 * 
	 * @author linjie
	 * @since 1.0.1
	 */
	ISqlQuery and(ISqlQuery query);
	
	/**
	 * 以OR连接关系添加下一个查询条件
	 * <br/> 如果在其中添加了多个条件, 则生成成组的查询, 查询条件外部被'()'包围.
	 * 
	 * @param query 查询条件, 不能为null
	 * @return 指向下一个查询条件的实例, 即传入的查询条件实例, 方便构建链式调用
	 * 
	 * @author linjie
	 * @since 1.0.1
	 */
	ISqlQuery or(ISqlQuery query);
	
	/**
	 * 与上一个查询条件的连接关系, 如果为第一个连接条件则为null
	 * 
	 * @return 与上一个查询条件的连接关系, 可能为null
	 * 
	 * @author linjie
	 * @since 1.0.1
	 */
	SqlQueryRelation getRelation();
	
	/**
	 * 获取查询的属性名称
	 * 
	 * @return 查询的属性名称, 可能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String getPropertyName();
	
	/**
	 * 获取查询的参数对象
	 * 
	 * @return 查询的参数对象数组, 可能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Object[] getQueryArgs();
	
	/**
	 * 获取对应使用的查询处理器
	 * 
	 * @return 对应使用的查询处理器
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlQueryStrategy getUsingStrategy();

	/**
	 * 查询条件的连接关系
	 * 
	 * @author linjie
	 * @since 1.0.1
	 */
	public enum SqlQueryRelation {
		
		/**
		 * AND连接关系
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		AND,
		
		/**
		 * OR连接关系
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		OR;
	};
}
