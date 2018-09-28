package com.fy.sqlparam.map;

import java.util.List;

/**
 * SQL映射元信息
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlMapMeta {

	/**
	 * 获取SQL映射元信息的名称
	 * 
	 * @return SQL映射元信息的名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String getName();
	
	/**
	 * 添加包含的SQL成员
	 * 
	 * @param sqlPart SQL成员, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addSqlPart(ISqlPart sqlPart);
	
	/**
	 * 获取SQL映射元信息包含的SQL成员
	 * 
	 * @return 包含的SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	List<ISqlPart> getSqlParts();
}
