package com.fy.sqlparam.map;

import java.util.Set;

/**
 * SQL成员
 *  
 * @author linjie
 * @since 1.0.0
 */
public interface ISqlPart {
	
	/**
	 * 获取SQL成员的类型字符串
	 * 
	 * @return SQL成员的类型字符串, 可能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String getType();
	
	/**
	 * 添加SQL成员依赖的映射元信息名称
	 * 
	 * @param name 依赖的映射元信息名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void addDependentMapMetaName(String name);
	
	/**
	 * 获取SQL成员依赖的所有映射元信息名称, 不会重复
	 * 
	 * @return 映射元信息名称集合, 如果没有则返回null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Set<String> getDependentMapMetaNames();
	
	/**
	 * 设置SQL成员使用的拼接处理器
	 * 
	 * @param joinStrategy 拼接处理器, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void setUsingJoinStrategy(ISqlJoinStrategy joinStrategy);
	
	/**
	 * 获取SQL成员使用的拼接处理器
	 * 
	 * @return 拼接处理器, 可能为null, 如果为null表示不需要拼接
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	ISqlJoinStrategy getUsingJoinStrategy();
	
	/**
	 * 设置映射字符串, 按该字符串生成映射键值对的名称
	 * 
	 * @return 映射字符串, 一般是正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void setAssignedMapStr(String assignedMapStr);
	
	/**
	 * 获取映射字符串, 按该字符串生成映射键值对的名称
	 * 
	 * @return 映射字符串, 一般是正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String getAssignedMapStr();
	
	/**
	 * SQL成员的SQL内容
	 * 
	 * @return SQL内容, 不会为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	StringBuilder getContent();
	
	/**
	 * SQL成员的SQL内容中的参数对象数组
	 * 
	 * @return SQL内容中的参数对象数组, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Object[] getArgObjs();
	
	/**
	 * 添加额外的参数
	 * 
	 * @param name 参数名称, 不能为null
	 * @param obj 对应的参数对象, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	void putExtra(String name, Object obj);
	
	/**
	 * 根据名称获取额外的参数
	 * 
	 * @param name 参数名称, 不能为null
	 * @return 对应的参数对象, 如果没有则返回null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Object getExtra(String name);
}
