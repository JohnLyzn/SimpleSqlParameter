package com.fy.sqlparam.map;


/**
 * SQL拼接处理器
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlJoinStrategy {

	/**
	 * 拼接SQL
	 * 
	 * @param source 源SQL成员
	 * @param other 拼接的SQL成员
	 * @param args 处理时使用的参数, 可以没有
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void joinSqlPart(ISqlPart source, ISqlPart other, Object...args);
	
	/**
	 * 拼接SQL中的占位参数
	 * 
	 * @param otherArgObjs 其它参数对象
	 * @param sqlPart 需要拼接SQL中的占位参数的SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.3
	 */
	Object[] joinArgObjs(Object[] otherArgObjs, ISqlPart sqlPart);
}
