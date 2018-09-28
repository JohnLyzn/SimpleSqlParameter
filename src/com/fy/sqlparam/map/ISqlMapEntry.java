package com.fy.sqlparam.map;

/**
 * SQL映射键值对
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlMapEntry {
	
	/**
	 * 键, 一般是正则表达式
	 * 
	 * @return 映射键值对的键
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String getMapStr();
	
	/**
	 * 值, 一般是SQL语句片段
	 * 
	 * @return 映射键值对的值
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlPart getSqlPart();
	
	/**
	 * 映射到SQL语句中
	 * 
	 * @param rawSql 需要进行映射的待处理SQL, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	boolean actMapping(StringBuilder rawSql);
}
