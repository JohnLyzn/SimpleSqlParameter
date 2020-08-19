package com.fy.sqlparam.map;

/**
 * SQL成员类型对应的处理器
 * 
 * @author linjie
 * @since 1.0.3
 */
public interface ISqlPartType {
	
	/**
	 * 获取SQL成员类型指定的SQL映射字符串, 可以为null
	 * 
	 * @return 指定的SQL映射字符串, 可以为null
	 * 
	 * @author linjie
	 * @since 1.0.3
	 */
	String getBasicAssignedMapStr();

	/**
	 * 在开始映射前格式化相应的SQL成员
	 * 
	 * @param rawSql 准备映射的源SQL模板
	 * @param sqlPart 当前需要格式化的SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.3
	 */
	void formatBeforeMapping(StringBuilder rawSql, ISqlPart sqlPart);
}
