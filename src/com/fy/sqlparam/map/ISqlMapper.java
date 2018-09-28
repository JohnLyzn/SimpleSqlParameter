package com.fy.sqlparam.map;

/**
 * SQL映射器
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlMapper {

	/**
	 * 映射SQL生成映射结果
	 * 
	 * @param mapContext 需要处理的映射上下文, 不能为null
	 * @param targetSql 需要处理的目标SQL, 不能为null
	 * @return SQL映射结果, 不会为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void map(ISqlMapContext mapContext, ISqlPart sqlPart);
}
	