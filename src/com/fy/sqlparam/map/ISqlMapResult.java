package com.fy.sqlparam.map;

/**
 * SQL映射结果
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlMapResult {

	/**
	 * 获取处理好的SQL语句
	 * 
	 * @return 处理好的SQL语句
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String getSql();
	
	/**
	 * 获取处理好的SQL参数对象数组
	 * 
	 * @return 处理好的SQL参数对象数组
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Object[] getArgObjs();
}
