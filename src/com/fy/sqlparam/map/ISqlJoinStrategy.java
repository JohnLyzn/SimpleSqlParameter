package com.fy.sqlparam.map;


/**
 * SQL拼接处理器
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlJoinStrategy {

	/**
	 * 拼接处理
	 * 
	 * @param source 源SQL成员
	 * @param other 拼接的SQL成员
	 * @param args 处理时使用的参数, 可以没有
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void handle(ISqlPart source, ISqlPart other, Object...args);
}
