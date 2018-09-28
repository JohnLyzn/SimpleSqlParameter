package com.fy.sqlparam.param;

import com.fy.sqlparam.map.ISqlMapContext;

/**
 * SQL查询处理器
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlQueryStrategy {

	/**
	 * 处理查询
	 * <br/> 把查询内容转换为SQL成员加入到映射上下文中
	 * 
	 * @param mapContext 映射上下文, 不能为null
	 * @param query 查询实例, 不能为null
	 * @param args 处理时使用的参数, 可以没有
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args);
}
