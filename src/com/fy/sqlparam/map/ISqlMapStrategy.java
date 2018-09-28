package com.fy.sqlparam.map;


/**
 * SQL映射处理器
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlMapStrategy {
	
	/**
	 * 映射处理
	 * 
	 * @param mapContext SQL映射上下文
	 * @param sqlPart 待处理的SQL成员
	 * @param args 处理时使用的参数, 可以没有
	 * @return 处理好的SQL参数对象数组
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void handle(ISqlMapContext mapContext, ISqlPart sqlPart, Object...args);
}
