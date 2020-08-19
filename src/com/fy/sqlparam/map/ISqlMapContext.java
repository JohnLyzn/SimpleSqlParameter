package com.fy.sqlparam.map;

import java.util.List;

import com.fy.sqlparam.param.ISqlParameterContext;

/**
 * SQL映射上下文
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlMapContext {

	/**
	 * 获取所属的搜索参数上下文
	 * 
	 * @return 当前映射上下文所属的搜索参数上下文, 不会返回null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlParameterContext getParamterContext();
	
	/**
	 * 添加当前SQL映射上下文支持的映射元信息
	 * 
	 * @param mapMeta SQL映射元信息, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addMapMeta(ISqlMapMeta mapMeta);
	
	/**
	 * 移除当前SQL映射上下文支持的映射元信息
	 * 
	 * @param mapMeta SQL映射元信息, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.3
	 */
	void removeMapMeta(ISqlMapMeta mapMeta);
	
	/**
	 * 根据映射元信息的名称获取SQL映射元信息
	 * 
	 * @param name 映射元信息的名称
	 * @return SQL映射元信息, 找不到返回null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlMapMeta getMapMetaByName(String name);
	
	/**
	 * 添加SQL成员到映射上下文
	 * 
	 * @param sqlPart SQL成员, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addSqlPart(ISqlPart sqlPart);
	
	/**
	 * 从映射上下文中移除SQL成员
	 * 
	 * @param sqlPart SQL成员, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.3
	 */
	void removeSqlPart(ISqlPart sqlPart);
	
	/**
	 * 获取映射上下文的所有SQL成员
	 * 
	 * @return SQL成员列表, 如果找不到返回空列表
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	List<ISqlPart> getSqlParts();
	
	/**
	 * 添加映射键值对到映射上下文
	 * 
	 * @param mapEntry 映射键值对, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addMapEntry(ISqlMapEntry mapEntry);
	
	/**
	 * 从映射上下文中移除映射键值对
	 * 
	 * @param mapEntry 映射键值对, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void removeMapEntry(ISqlMapEntry mapEntry);
	
	/**
	 * 判断依赖的映射元信息是否已经处理过
	 * 
	 * @param name 需要判断的映射元信息的名称
	 * @return 依赖的映射元信息是否已经处理过, 已处理返回<tt>true</tt>, 未处理返回<tt>false</tt>
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	boolean hasHandleDependentMapMeta(String name);
	
	/**
	 * 通知处理依赖的映射元信息, 使当前映射上下文加入相应的SQL成员
	 * 
	 * @param srcSqlPart 要求触发该处理的SQL成员
	 * @param name 需要处理的映射元信息的名称
	 * @return 对应名称的映射元信息实例, 如果不存在该映射元信息或已经处理过了, 则返回null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlMapMeta notifyHandleDependentMapMeta(ISqlPart srcSqlPart, String name);
	
	/**
	 * 获取映射结果, 由外部指定实现的映射处理器
	 * 
	 * @param rawSql 待处理的SQL语句, 不能为null
	 * @return 映射结果, 包含映射完成的SQL语句和参数对象数组
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlMapResult generateMapResult(String rawSql);
}
