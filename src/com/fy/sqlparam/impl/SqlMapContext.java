package com.fy.sqlparam.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fy.sqlparam.impl.SqlParameter.SqlPart;
import com.fy.sqlparam.map.ISqlJoinStrategy;
import com.fy.sqlparam.map.ISqlMapContext;
import com.fy.sqlparam.map.ISqlMapEntry;
import com.fy.sqlparam.map.ISqlMapMeta;
import com.fy.sqlparam.map.ISqlMapResult;
import com.fy.sqlparam.map.ISqlMapper;
import com.fy.sqlparam.map.ISqlPart;
import com.fy.sqlparam.map.ISqlPartType;
import com.fy.sqlparam.param.ISqlParameterContext;
import com.fy.sqlparam.param.ISqlQuery.SqlQueryRelation;
import com.fy.sqlparam.util.FormatUtils;

/**
 * SQL映射上下文, 每次映射产生一个实例
 * 
 * @author linjie
 * @since 1.0.0
 */
public class SqlMapContext implements ISqlMapContext {
	
	/**
	 * 所属的搜索参数上下文
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final ISqlParameterContext belongParameterContext;

	/**
	 * 所使用的SQL映射器
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final ISqlMapper sqlMapper;
	
	/**
	 * 当前映射上下文临时使用的映射元信息表, key是映射元信息的名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final Map<String, ISqlMapMeta> tempMapMetaMap = new HashMap<String, ISqlMapMeta>();
	
	/**
	 * 当前映射上下文中包含的所有SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final Set<ISqlPart> allSqlParts = new LinkedHashSet<ISqlPart>();
	
	/**
	 * 当前映射上下文中包含的SQL成员表, 主要用于同类型的SQL成员拼接
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final Map<String, Set<ISqlPart>> joinableSqlPartMap = new HashMap<String, Set<ISqlPart>>();
	
	/**
	 * 当前映射上下文中包含的映射键值对
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final List<ISqlMapEntry> mapEntries = new LinkedList<ISqlMapEntry>();
	
	/**
	 * 当前映射上下文中已经处理过了映射元信息的名称
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final Set<String> handledMapMetaNames = new HashSet<String>();
	
	/**
	 * 构造器, 初始化映射上下文: 指定所属的搜索参数上下文, 同时加入默认映射内容和SQL内容
	 * 
	 * @param belongParameterContext 所属的搜索参数上下文, 不能为null
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public SqlMapContext(ISqlParameterContext belongParameterContext, ISqlMapper sqlMapper) {
		if(belongParameterContext == null) {
			throw new IllegalArgumentException("生成SQL映射上下文时必须指定所属的SQL搜索参数上下文");
		}
		if(sqlMapper == null) {
			throw new IllegalArgumentException("生成SQL映射上下文时必须指定所属的使用的SQL映射器");
		}
		this.sqlMapper = sqlMapper;
		this.belongParameterContext = belongParameterContext;
		// 加入默认映射内容
		List<ISqlMapEntry> defaultMapEntries = this.belongParameterContext.getDefaultMapEntries();
		for(ISqlMapEntry defaultMapEntry : defaultMapEntries) {
			this.addMapEntry(defaultMapEntry);
		}
		// 加入默认SQL内容
		List<ISqlPart> defaultSqlParts = this.belongParameterContext.getDefaultSqlParts();
		for(ISqlPart defaultSqlPart : defaultSqlParts) {
			this.addSqlPart(defaultSqlPart);
		}
	}
	
	@Override
	public ISqlParameterContext getParamterContext() {
		return this.belongParameterContext;
	}
	
	@Override
	public void addMapMeta(ISqlMapMeta mapMeta) {
		if(mapMeta == null) {
			throw new IllegalArgumentException("添加的临时映射元信息不能为null");
		}
		if(mapMeta.getName() == null) {
			throw new IllegalArgumentException("添加的临时映射元信息的名称不能为null");
		}
		this.tempMapMetaMap.put(mapMeta.getName(), mapMeta);
	}

	@Override
	public ISqlMapMeta getMapMetaByName(String name) {
		ISqlMapMeta result = this.tempMapMetaMap.get(name);
		if(result == null) {
			result = this.getMapMetaFromParamContext(name);
		}
		return result;
	}
	
	@Override
	public void removeMapMeta(ISqlMapMeta mapMeta) {
		if(mapMeta == null || ! this.tempMapMetaMap.containsValue(mapMeta)) {
			return;
		}
		this.tempMapMetaMap.remove(mapMeta.getName(), mapMeta);
	}

	@Override
	public void addSqlPart(ISqlPart sqlPart) {
		if(sqlPart == null) {
			throw new IllegalArgumentException("添加的SQL成员不能为null");
		}
		this.addSqlPart0(sqlPart);
	}
	
	@Override
	public void removeSqlPart(ISqlPart sqlPart) {
		if(sqlPart == null) {
			return;
		}
		// 如果就在列表里, 直接删除
		if(this.allSqlParts.contains(sqlPart)) {
			this.allSqlParts.remove(sqlPart);
		}
		// 如果是可拼接SQL成员的一部分, 要把这部分也删除掉
		Set<ISqlPart> joinableSqlParts = this.joinableSqlPartMap.get(sqlPart.getType());
		if(joinableSqlParts != null 
			&& joinableSqlParts.contains(sqlPart)) {
			joinableSqlParts.remove(sqlPart);
		}
		// 如果存在由此SQL成员生成的映射键值对, 要把它删掉
		List<ISqlMapEntry> needRemoveEntries = new LinkedList<ISqlMapEntry>();
		for(ISqlMapEntry mapEntry : this.mapEntries) {
			if(mapEntry.getSqlPart().equals(sqlPart)) {
				needRemoveEntries.add(mapEntry);
			}
		}
		this.mapEntries.removeAll(needRemoveEntries);
	}
	
	@Override
	public List<ISqlPart> getSqlParts() {
		return Collections.unmodifiableList(Arrays.asList(
				this.allSqlParts.toArray(new ISqlPart[this.allSqlParts.size()])));
	}
	
	@Override
	public void addMapEntry(ISqlMapEntry mapEntry) {
		if(mapEntry == null) {
			throw new IllegalArgumentException("添加的SQL映射信息对不能为null");
		}
		mapEntries.add(mapEntry);
	}
	
	@Override
	public void removeMapEntry(ISqlMapEntry mapEntry) {
		if(mapEntry == null || mapEntries.indexOf(mapEntry) == -1) {
			return;
		}
		mapEntries.remove(mapEntry);
	}
	
	@Override
	public boolean hasHandleDependentMapMeta(String name) {
		return this.handledMapMetaNames.contains(name);
	}
	
	@Override
	public ISqlMapMeta notifyHandleDependentMapMeta(ISqlPart srcSqlPart, String name) {
		ISqlMapMeta mapMeta = this.getMapMetaByName(name);
		if(mapMeta == null) {
			return null;
		}
		if(! mapMeta.accept(srcSqlPart.getType())) {
			throw new IllegalArgumentException(String.format(
					"映射元[%s]不支持在%s中使用", name, srcSqlPart.getType()));
		}
		if(this.handledMapMetaNames.contains(name)) {
			return null;
		}
		this.handledMapMetaNames.add(name);
		List<ISqlPart> includeSqlParts = mapMeta.getSqlParts();
		for(ISqlPart includeSqlPart : includeSqlParts) {
			this.addSqlPart0(includeSqlPart);
		}
		return mapMeta;
	}
	
	@Override
	public ISqlMapResult generateMapResult(String rawSql) {
		// 把源SQL处理为SQL成员, 处理其中的映射字符串
		StringBuilder target = new StringBuilder(rawSql);
		this.addSqlPart(new SqlPart(null, target));
		// 按SQL成员类型进行一些格式化和拼接处理
		for(SqlPartType type : SqlPartType.values()) {
			this.handleDynamicalChangeSQL(target, type,
					this.joinableSqlPartMap.get(type.name()));
		}
		// 对生成的映射键值对调用映射处理, 一直处理到不再可映射为止
		boolean lastHandled = true;
		while(target.indexOf("{") != -1 && lastHandled) {
			lastHandled = false;
			for(ISqlMapEntry mapEntry : this.mapEntries) {
				if(mapEntry.actMapping(target)) {
					lastHandled = true;
				}
			}
		}
		// 把剩余的无对应映射信息的所有特殊字符串(被'{}'包住的)消除掉
		SqlMapper.wipeRegExpStrWhenNoMapEntries(target, SqlMapper.REGEXP_ALL, null);
		// 拼接所有参数数组
		Object[] argObjs = null;
		for(ISqlPart sqlPart : this.allSqlParts) {
			argObjs = this.joinAllSqlPartArgObjs(argObjs, sqlPart);
		}
		// 返回结果
		return new SqlMapResult(target.toString(), argObjs);
	}

	/**
	 * SQL映射元信息的实现
	 * <br/>利用enum来单例, 同时减少类文件
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static class SqlMapMeta implements ISqlMapMeta {
		
		/**
		 * 映射元信息的名称
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final String name;
		
		/**
		 * 严格接收作为指定类型的SQL成员
		 * 
		 * @author linjie
		 * @since 1.0.3
		 */
		private final String acceptTypes;
		
		/**
		 * 包含的SQL成员
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final List<ISqlPart> includeSqlParts = new LinkedList<ISqlPart>();

		public SqlMapMeta(String name, String acceptTypes) {
			this.name = name;
			this.acceptTypes = acceptTypes;
		}
		
		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public void addSqlPart(ISqlPart sqlPart) {
			this.includeSqlParts.add(sqlPart);
		}

		@Override
		public List<ISqlPart> getSqlParts() {
			return this.includeSqlParts;
		}

		@Override
		public boolean accept(String type) {
			if(type == null || this.acceptTypes == null) {
				return true;
			}
			return acceptTypes.indexOf(type) != -1;
		}
	}
	
	/**
	 * SQL映射结果
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static class SqlMapResult implements ISqlMapResult {
		
		/**
		 * 完整的SQL语句
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final String sql;
		
		/**
		 * 完整的SQL语句中的参数对象数组
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final Object[] argObjs;
		
		/**
		 * 构造SQL映射结果实例
		 * 
		 * @param sql 完整的SQL语句
		 * @param argObjs 完整的SQL语句中的参数对象数组
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public SqlMapResult(String sql, Object[] argObjs) {
			this.sql = sql;
			this.argObjs = argObjs;
		}
		
		@Override
		public String getSql() {
			return this.sql;
		}

		@Override
		public Object[] getArgObjs() {
			if(this.argObjs == null) {
				return Collections.emptyList().toArray();
			}
			return this.argObjs;
		}
	}
	
	/**
	 * SQL成员类型
	 * 
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public enum SqlPartType {
		
		/**
		 * SQL语句中的SELECT部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		SELECT(new ISqlPartType() {
			@Override
			public String getBasicAssignedMapStr() {
				return SqlMapper.REGEXP_SELECT;
			}

			@Override
			public void formatBeforeMapping(StringBuilder rawSql, ISqlPart sqlPart) {
				if(sqlPart == null) {
					return;
				}
				sqlPart.getContent().insert(0, ",");
			}
		}),
		
		/**
		 * SQL语句中的FROM_TABLES部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		FROM_TABLES(new ISqlPartType() {
			@Override
			public String getBasicAssignedMapStr() {
				return SqlMapper.REGEXP_EXTRA_TABLES;
			}

			@Override
			public void formatBeforeMapping(StringBuilder rawSql, ISqlPart sqlPart) {
				
			}
		}),
		
		/**
		 * SQL语句中的CONDITIONS部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		WHERE(new ISqlPartType() {
			@Override
			public String getBasicAssignedMapStr() {
				return SqlMapper.REGEXP_CONDITIONS;
			}

			@Override
			public void formatBeforeMapping(StringBuilder rawSql, ISqlPart sqlPart) {
				if(sqlPart != null && sqlPart.getContent().length() > 0) {
					return;
				}
				/* 如果没有查询条件, 则默认加上一个没意义的条件1=1使得后面拼接的SQL内容不会出错 */
				SqlMapper.findAndReplaceContentByRegExpStr(rawSql,
						"WHERE(?=([ ]+\\{))", "WHERE 1=1 ");
			}
		}),
		
		/**
		 * SQL语句中的GROUP_BY部分
		 * @author linjie
		 * @since 1.0.0
		 */
		GROUP_BY(new ISqlPartType() {
			@Override
			public String getBasicAssignedMapStr() {
				return null;
			}

			@Override
			public void formatBeforeMapping(StringBuilder rawSql, ISqlPart sqlPart) {
				
			}
		}),
		
		/**
		 * SQL语句中的ORDER_BY部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		ORDER_BY(new ISqlPartType() {
			@Override
			public String getBasicAssignedMapStr() {
				return SqlMapper.REGEXP_ORDER_BY;
			}

			@Override
			public void formatBeforeMapping(StringBuilder rawSql, ISqlPart sqlPart) {
				if(sqlPart == null) {
					return;
				}
				sqlPart.getContent().insert(0, "ORDER BY ");
			}
		}),
		
		/**
		 * SQL语句中的LIMIT部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		LIMIT(new ISqlPartType() {
			@Override
			public String getBasicAssignedMapStr() {
				return SqlMapper.REGEXP_LIMIT;
			}

			@Override
			public void formatBeforeMapping(StringBuilder rawSql, ISqlPart sqlPart) {
				
			}
		});
		
		
		/**
		 * SQL成员处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final ISqlPartType handler;
		
		/**
		 * 内部构造器: 提供SQL成员处理器的实例
		 * 
		 * @param strategy SQL成员处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private SqlPartType(ISqlPartType handler) {
			this.handler = handler;
		}
		
		/**
		 * 获取SQL成员处理器的实例
		 * 
		 * @return SQL成员处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public ISqlPartType instance() {
			return this.handler;
		}
		
		
		/**
		 * 根据名字获取枚举量, 如果找不到不会抛出异常
		 * @param name
		 * @return
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public static SqlPartType valueOfWithoutException(String name) {
			if(name == null || name.isEmpty()) {
				return null;
			}
			for(SqlPartType element : SqlPartType.values()) {
				if(element.name().equals(name)) {
					return element;
				}
			}
			return null;
		}
	}
	
	/**
	 * SQL拼接处理
	 * <br/>利用enum来单例, 同时减少类文件
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public enum SqlJoinStrategy {
		
		/**
		 * 拼接字段返回SQL的处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		JOIN_SELECT(new ISqlJoinStrategy() {

			@Override
			public void joinSqlPart(ISqlPart source, ISqlPart other, Object...args) {
				SqlJoinStrategy.joinSqlParts(source, ",", other);
			}

			@Override
			public Object[] joinArgObjs(Object[] otherArgObjs, ISqlPart sqlPart) {
				return otherArgObjs;
			}
		}),
		
		/**
		 * 拼接关联表SQL的处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		JOIN_JOINTABLES(new ISqlJoinStrategy() {

			@Override
			public void joinSqlPart(ISqlPart source, ISqlPart other, Object...args) {
				StringBuilder rawSql = (StringBuilder) (args != null ? args[0] : null);
				// 相同的连接表不重复加入
				if(rawSql != null && rawSql.indexOf(other.getContent().toString()) != -1) {
					return;
				}
				// 包括目前要拼接的也不能重复
				if(source != null && source.getContent().indexOf(other.getContent().toString()) != -1) {
					return;
				}
				SqlJoinStrategy.joinSqlParts(source, " ", other);
			}

			@Override
			public Object[] joinArgObjs(Object[] otherArgObjs, ISqlPart sqlPart) {
				return SqlJoinStrategy.joinSqlPartArgObjs(otherArgObjs, sqlPart);
			}
		}),
		
		/**
		 * 拼接条件SQL的处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		JOIN_CONDITIONS(new ISqlJoinStrategy() {

			@Override
			public void joinSqlPart(ISqlPart source, ISqlPart other, Object...args) {
				SqlQueryRelation relation = (SqlQueryRelation) other.getExtra("relation");
				String middleStr = relation == null ? "" 
						: SqlQueryRelation.AND.equals(relation) ? " AND " : " OR ";
				SqlJoinStrategy.joinSqlParts(source, middleStr, other);
			}

			@Override
			public Object[] joinArgObjs(Object[] otherArgObjs, ISqlPart sqlPart) {
				return SqlJoinStrategy.joinSqlPartArgObjs(otherArgObjs, sqlPart);
			}
		}),
		
		/**
		 * 拼接排序SQL的处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		JOIN_ORDERBYS(new ISqlJoinStrategy() {
			
			@Override
			public void joinSqlPart(ISqlPart source, ISqlPart other, Object...args) {
				SqlJoinStrategy.joinSqlParts(source, ", ", other);
			}

			@Override
			public Object[] joinArgObjs(Object[] otherArgObjs, ISqlPart sqlPart) {
				return otherArgObjs;
			}
		});
		
		/**
		 * 拼接处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final ISqlJoinStrategy strategy;
		
		/**
		 * 内部构造器: 提供拼接处理器的实例
		 * 
		 * @param strategy 拼接处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private SqlJoinStrategy(ISqlJoinStrategy strategy) {
			this.strategy = strategy;
		}
		
		/**
		 * 获取拼接处理器的实例
		 * 
		 * @return 拼接处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public ISqlJoinStrategy instance() {
			return this.strategy;
		}
		
		/**
		 * 辅助函数: 拼接SQL成员的实现
		 * <br/> 注意: 拼接SQL就行, 不要拼接参数数组, 防止删除SQL成员时困难
		 * 
		 * @param source 源SQL成员
		 * @param middleStr 中间拼接字符串
		 * @param others 拼接的SQL成员, 可以有多个
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static void joinSqlParts(ISqlPart source, String middleStr, ISqlPart...others) {
			if(source != null && others.length > 0) {
				StringBuilder sourceSql = source.getContent();
				for(ISqlPart other : others) {
					// 拼接sql
					if(sourceSql.length() != 0 && middleStr != null && ! middleStr.isEmpty()) {
						sourceSql.append(middleStr);
					}
					sourceSql.append(other.getContent());
				}
			}
		}
		
		/**
		 * 辅助函数: 仅拼接SQL成员的对象数组
		 * 
		 * @param otherArgObjs 已经有的参数对象数组
		 * @param sqlPart 要加上的参数对象数组所属的SQL成员
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static Object[] joinSqlPartArgObjs(Object[] otherArgObjs, ISqlPart sqlPart) {
			if(sqlPart == null) {
				return otherArgObjs;
			}
			Object[] argObjs = sqlPart.getArgObjs();
			if(argObjs == null) {
				return otherArgObjs;
			}
			if(otherArgObjs == null) {
				return argObjs;
			}
			return FormatUtils.concatArrays(otherArgObjs, argObjs);
		}
	}
	
	/**
	 * 根据名称从搜索参数上下文中获取映射元信息
	 * 
	 * @param name 映射元信息的名称
	 * @throws IllegalArgumentException 找不到则抛出异常
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private ISqlMapMeta getMapMetaFromParamContext(String name) {
		ISqlMapMeta result = this.belongParameterContext.getDefaultMapMetaByName(name);
		if(result == null) {
			throw new IllegalArgumentException(String.format("找不到名称为%s的映射元信息", name));
		}
		return result;
	}
	
	/**
	 * 按类型设置SQL成员的映射字符串
	 * 
	 * @param sqlPart SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private void setMapStr4BasicSqlPart(SqlPartType type, ISqlPart sqlPart) {
		final String assignedMapStr = type.instance().getBasicAssignedMapStr();
		if(assignedMapStr == null) {
			return;
		}
		sqlPart.setAssignedMapStr(assignedMapStr);
	}
	
	/**
	 * 处理会动态变化的SQL内容
	 * 
	 * @param rawSql 需要处理的源SQL
	 * @param type SQL内容的类型, 见{@link SqlPartType}
	 * @param sqlParts 对应该类型的待拼接SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private void handleDynamicalChangeSQL(StringBuilder rawSql,
			SqlPartType type, Set<ISqlPart> sqlParts) {
		if(sqlParts == null || sqlParts.isEmpty()) {
			return;
		}
		// 先进行处理前的格式化
		for(ISqlPart sqlPart : sqlParts) {
			type.instance().formatBeforeMapping(rawSql, sqlPart);
		}
		// 建立一个空的SQL成员, 通过不断与第一个SQL成员合并完成拼接
		ISqlPart source = new SqlPart(null, new StringBuilder());
		// 根据类型设置基本映射的映射字符串
		this.setMapStr4BasicSqlPart(type, source);
		// 拼接SQL成员
		for(ISqlPart sqlPart : sqlParts) {
			ISqlJoinStrategy joinStrategy = sqlPart.getUsingJoinStrategy();
			joinStrategy.joinSqlPart(source, sqlPart, rawSql);
		}
		// 触发映射处理
		this.sqlMapper.map(this, source);
	}
	
	/**
	 * 
	 * @param otherArgObjs
	 * @param sqlPart
	 * @return
	 */
	private Object[] joinAllSqlPartArgObjs(Object[] otherArgObjs, ISqlPart sqlPart) {
		if(sqlPart == null) {
			return otherArgObjs;
		}
		final ISqlJoinStrategy joinStragy = sqlPart.getUsingJoinStrategy();
		if(joinStragy == null) {
			return otherArgObjs;
		}
		return joinStragy.joinArgObjs(otherArgObjs, sqlPart);
	}
	
	/**
	 * 添加SQL成员到当前映射上下文的实现
	 * 
	 * @param sqlPart SQL成员
	 * @throws IllegalArgumentException 添加的SQL成员不符合规则则抛出异常
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private void addSqlPart0(ISqlPart sqlPart) {
		// SQL成员的依赖处理
		Set<String> dependentMapMetaNames = sqlPart.getDependentMapMetaNames();
		if(dependentMapMetaNames != null && ! dependentMapMetaNames.isEmpty()) {
			for(String dependentMapMetaName : dependentMapMetaNames) {
				this.notifyHandleDependentMapMeta(sqlPart, dependentMapMetaName);
			}
		}
		// 获取其类型和相应类型的处理器
		String typeStr = sqlPart.getType();
		SqlPartType type = SqlPartType.valueOfWithoutException(typeStr);
		// 不带类型的SQL成员, 直接进行生成映射键值对的处理后加入所有缓存
		if(typeStr == null || type == null) {
			this.sqlMapper.map(this, sqlPart);
			this.allSqlParts.add(sqlPart);
			return;
		}
		// 获取拼接策略
		ISqlJoinStrategy joinStrategy = sqlPart.getUsingJoinStrategy();
		// 不需要拼接的直接处理完成即可
		if(joinStrategy == null) {
			this.sqlMapper.map(this, sqlPart);
			this.allSqlParts.add(sqlPart);
			return;
		}
		// 需要拼接的按类型加入可拼接SQL成员的缓存中
		Set<ISqlPart> joinableSqlParts = this.joinableSqlPartMap.get(typeStr);
		if(joinableSqlParts == null) {
			// 生成对应的容器
			joinableSqlParts = new LinkedHashSet<ISqlPart>();
			// 每种SQL成员类型只存储一个容器SQL成员, 不断往这个容器SQL成员拼接内容来完成结果生成
			this.joinableSqlPartMap.put(typeStr, joinableSqlParts);
		}
		// 添加到拼接池中
		joinableSqlParts.add(sqlPart);
		// 进行映射处理
		this.sqlMapper.map(this, sqlPart);
		// 添加到所有池中
		this.allSqlParts.add(sqlPart);
	}
}
