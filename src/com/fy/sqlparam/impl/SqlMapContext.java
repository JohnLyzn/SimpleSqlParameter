package com.fy.sqlparam.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fy.sqlparam.impl.SqlMapper.SqlMapStrategy;
import com.fy.sqlparam.impl.SqlParameter.SqlPart;
import com.fy.sqlparam.map.ISqlJoinStrategy;
import com.fy.sqlparam.map.ISqlMapContext;
import com.fy.sqlparam.map.ISqlMapEntry;
import com.fy.sqlparam.map.ISqlMapMeta;
import com.fy.sqlparam.map.ISqlMapResult;
import com.fy.sqlparam.map.ISqlMapper;
import com.fy.sqlparam.map.ISqlPart;
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
	private final List<ISqlPart> allSqlParts = new LinkedList<ISqlPart>();
	
	/**
	 * 当前映射上下文中包含的SQL成员表, 主要用于同类型的SQL成员拼接
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private final Map<SqlPartType, ISqlPart> joinableSqlPartMap = new HashMap<SqlPartType, ISqlPart>();
	
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
	public List<ISqlPart> getSqlParts() {
		return Collections.unmodifiableList(this.allSqlParts);
	}

	@Override
	public void addSqlPart(ISqlPart sqlPart) {
		if(sqlPart == null) {
			throw new IllegalArgumentException("添加的SQL成员不能为null");
		}
		this.addSqlPart0(sqlPart);
	}
	
	@Override
	public void addMapEntry(ISqlMapEntry mapEntry) {
		if(mapEntry == null) {
			throw new IllegalArgumentException("添加的SQL映射信息对不能为null");
		}
		mapEntries.add(mapEntry);
	}
	
	@Override
	public boolean hasHandleDependentMapMeta(String name) {
		return this.handledMapMetaNames.contains(name);
	}
	
	@Override
	public ISqlMapMeta notifyHandleDependentMapMeta(String name) {
		if(this.handledMapMetaNames.contains(name)) {
			return null;
		}
		ISqlMapMeta mapMeta = this.getMapMetaByName(name);
		if(mapMeta == null) {
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
		// 把源SQL作为SQL成员加入, 处理其中的映射字符串
		StringBuilder target = new StringBuilder(rawSql);
		this.addSqlPart(new SqlPart(null, target));
		// 格式化某些可能动态变化的容器SQL成员
		this.formatDynamicalChangeSQL(target, SqlPartType.WHERE,
				this.joinableSqlPartMap.get(SqlPartType.WHERE));
		this.formatDynamicalChangeSQL(target, SqlPartType.ORDER_BY,
				this.joinableSqlPartMap.get(SqlPartType.ORDER_BY));
		// 对生成的映射键值对调用映射处理
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
		argObjs = SqlJoinStrategy.joinSqlPartArgObjs(argObjs,
				this.joinableSqlPartMap.get(SqlPartType.FROM_TABLES));
		argObjs = SqlJoinStrategy.joinSqlPartArgObjs(argObjs,
				this.joinableSqlPartMap.get(SqlPartType.WHERE));
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
		 * 包含的SQL成员
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final List<ISqlPart> includeSqlParts = new LinkedList<ISqlPart>();

		public SqlMapMeta(String name) {
			this.name = name;
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
		SELECT,
		
		/**
		 * SQL语句中的FROM_TABLES部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		FROM_TABLES,
		
		/**
		 * SQL语句中的CONDITIONS部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		WHERE,
		
		/**
		 * SQL语句中的GROUP_BY部分
		 * @author linjie
		 * @since 1.0.0
		 */
		GROUP_BY,
		
		/**
		 * SQL语句中的ORDER_BY部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		ORDER_BY,
		
		/**
		 * SQL语句中的LIMIT部分
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		LIMIT;
		
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
		 * 拼接关联表SQL的处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		JOIN_JOINTABLES(new ISqlJoinStrategy() {

			@Override
			public void handle(ISqlPart source, ISqlPart other, Object...args) {
				SqlJoinStrategy.joinSqlParts(source, " ", other);
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
			public void handle(ISqlPart source, ISqlPart other, Object...args) {
				SqlQueryRelation relation = (SqlQueryRelation) other.getExtra("relation");
				String middleStr = relation == null ? "" 
						: SqlQueryRelation.AND.equals(relation) ? " AND " : " OR ";
				SqlJoinStrategy.joinSqlParts(source, middleStr, other);
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
			public void handle(ISqlPart source, ISqlPart other, Object...args) {
				SqlJoinStrategy.joinSqlParts(source, ", ", other);
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
				SqlPart sourceReal = (SqlPart) source;
				StringBuilder sourceSql = source.getContent();
				Object[] sourceArgObjs = source.getArgObjs();
				for(ISqlPart other : others) {
					// 拼接sql
					if(sourceSql.length() != 0 && middleStr != null && ! middleStr.isEmpty()) {
						sourceSql.append(middleStr);
					}
					sourceSql.append(other.getContent());
					// 拼接参数对象数组
					sourceArgObjs = SqlJoinStrategy.joinSqlPartArgObjs(sourceArgObjs, other);
				}
				sourceReal.setArgObjs(sourceArgObjs);
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
	 * 格式化某些会动态变化的SQL内容
	 * 
	 * @param rawSql 需要处理的源SQL
	 * @param type SQL内容的类型, 见{@link SqlPartType}
	 * @param containerSqlPart 对应该类型的容器SQL成员
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private void formatDynamicalChangeSQL(StringBuilder rawSql,
			SqlPartType type, ISqlPart containerSqlPart) {
		switch(type) {
		case WHERE:
			if(containerSqlPart == null) {
				/* 如果没有查询条件, 则默认加上一个没意义的条件1=1使得后面拼接的SQL内容不会出错 */
				SqlMapper.findAndReplaceContentByRegExpStr(rawSql,
						"WHERE(?=([ ]+\\{))", "WHERE 1=1 ");
			}
			break;
		case ORDER_BY:
			if(containerSqlPart != null) {
				containerSqlPart.getContent().insert(0, "ORDER BY ");
			}
			break;
		default:
			break;
		}
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
		switch(type) {
		case FROM_TABLES:
			sqlPart.setAssignedMapStr(SqlMapper.REGEXP_EXTRA_TABLES);
			break;
		case WHERE:
			sqlPart.setAssignedMapStr(SqlMapper.REGEXP_CONDITIONS);
			break;
		case ORDER_BY:
			sqlPart.setAssignedMapStr(SqlMapper.REGEXP_ORDER_BY);
			break;
		case LIMIT:
			sqlPart.setAssignedMapStr(SqlMapper.REGEXP_LIMIT);
			break;
		default:
			break;
		}
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
				this.notifyHandleDependentMapMeta(dependentMapMetaName);
			}
		}
		String typeStr = sqlPart.getType();
		SqlPartType type = SqlPartType.valueOfWithoutException(typeStr);
		// 1.带类型的SQL成员, 可能需要JOIN处理, 加入到按类型的表缓存中, 先不生成映射键值对
		if(typeStr != null && type != null) {
			// 设置其映射字符串
			this.setMapStr4BasicSqlPart(type, sqlPart);
			// 加入SQL成员的缓存中
			ISqlPart existSqlPart = this.joinableSqlPartMap.get(type);
			if(existSqlPart == null) {
				// 每种SQL成员类型只存储一个容器SQL成员, 不断往这个容器SQL成员拼接内容来完成结果生成生成
				existSqlPart = new SqlPart(sqlPart);
				this.allSqlParts.add(existSqlPart);
				this.joinableSqlPartMap.put(type, existSqlPart);
				// 这个容器SQL成员作为静态映射处理
				SqlMapStrategy.MAP_STATIC.instance().handle(this, existSqlPart); 
			}
			// 被拼接的SQL成员不能是静态映射处理
			sqlPart.setAssignedMapStr(null);
			this.sqlMapper.map(this, sqlPart);
			// 拼接SQL成员
			ISqlJoinStrategy joinStrategy = existSqlPart.getUsingJoinStrategy();
			if(joinStrategy != null) {
				joinStrategy.handle(existSqlPart, sqlPart);
			}
			return;
		}
		// 2.不带类型的SQL成员, 直接进行生成映射键值对的处理后加入所有缓存
		this.sqlMapper.map(this, sqlPart);
		this.allSqlParts.add(sqlPart);
	}
}
