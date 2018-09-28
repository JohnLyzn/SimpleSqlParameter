package com.fy.sqlparam.param;

import java.util.List;

import com.fy.sqlparam.map.ISqlMapEntry;
import com.fy.sqlparam.map.ISqlMapMeta;
import com.fy.sqlparam.map.ISqlPart;

/**
 * SQL搜索参数上下文
 * 
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlParameterContext {

	/**
	 * 添加当前SQL搜索参数上下文默认支持的映射元信息
	 * 
	 * @param mapMeta SQL映射元信息, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addDefaultMapMeta(ISqlMapMeta mapMeta);
	
	/**
	 * 根据映射元信息的名称获取SQL映射元信息
	 * 
	 * @param name 映射元信息的名称
	 * @return SQL映射元信息, 找不到返回null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlMapMeta getDefaultMapMetaByName(String name);
	
	/**
	 * 添加默认加入的SQL成员
	 * 
	 * @param sqlPart 默认加入的SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addDefaultSqlPart(ISqlPart sqlPart);
	
	/**
	 * 获取默认加入的SQL成员列表
	 * 
	 * @return 默认加入的SQL成员列表
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	List<ISqlPart> getDefaultSqlParts();
	
	/**
	 * 添加默认加入的映射键值对
	 * 
	 * @param mapEntry 映射键值对
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addDefaultMapEntry(ISqlMapEntry mapEntry);
	
	/**
	 * 获取默认加入的映射键值对列表
	 * 
	 * @return 默认加入的映射键值对列表
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	List<ISqlMapEntry> getDefaultMapEntries();
}
