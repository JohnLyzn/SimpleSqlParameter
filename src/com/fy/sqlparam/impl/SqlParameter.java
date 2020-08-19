package com.fy.sqlparam.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fy.sqlparam.impl.SqlMapContext.SqlJoinStrategy;
import com.fy.sqlparam.impl.SqlMapContext.SqlPartType;
import com.fy.sqlparam.map.ISqlJoinStrategy;
import com.fy.sqlparam.map.ISqlMapContext;
import com.fy.sqlparam.map.ISqlMapper;
import com.fy.sqlparam.map.ISqlPart;
import com.fy.sqlparam.param.ISqlParameter;
import com.fy.sqlparam.param.ISqlParameterContext;
import com.fy.sqlparam.param.ISqlQuery;
import com.fy.sqlparam.param.ISqlQueryGroup;
import com.fy.sqlparam.param.ISqlQueryMethod;
import com.fy.sqlparam.param.ISqlQueryStrategy;
import com.fy.sqlparam.util.FormatUtils;

/**
 * SQL搜索参数实现
 * 
 * @author linjie
 * @since 1.0.0
 */
public class SqlParameter implements ISqlParameter {

	/**
	 * 输出查询组
	 * 
	 * @author linjie
	 * @since 1.0.2
	 */
	private SqlQueryGroup selects = null;
	
	/**
	 * 条件查询组
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private SqlQueryGroup conditions = null;
	
	/**
	 * 排序查询组
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private SqlQueryGroup sorts = null;
	
	/**
	 * 分页查询
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private SqlQuery limit = null;
	
	/**
	 * 空查询条件, 用于需要开始就动态查询的情形
	 * 
	 * @author linjie
	 * @since 1.0.2
	 */
	private SqlQuery empty = null;
	
	/**
	 * 查询构建器
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static final class Query {
		
		/**
		 * 对哪个属性进行查询
		 * 
		 * @param propertyName 属性名称, 不能为null
		 * @return 该属性支持的查询方法
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public static ISqlQueryMethod to(String propertyName) {
			return new QueryMethod(propertyName);
		}
		
		/**
		 * 静态类不允许实例化
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private Query() {};
	}
	
	/**
	 * {@inheritDoc}
	 * <br/> 使用{@link Query}来构建查询条件, 例如: <p><code>parameter.query(Query.to("foo").eq("bar"));</code></p>
	 */
	@Override
	public ISqlQuery query(ISqlQuery query) {
		if(query == null) {
			throw new IllegalArgumentException("添加的查询条件不能为null");
		}
		SqlQuery queryReal = ((SqlQuery) query);
		// 未产生第一个条件的情况下
		if(this.conditions == null) {
			// 找到传入查询实例的根, 生成查询条件总的组
			while(queryReal.belongGroup != null) {
				queryReal = queryReal.belongGroup;
			}
			this.conditions = new SqlQueryGroup(queryReal);
			return this.conditions;
		}
		// 不是第一个就直接加入即可
		this.conditions.and(query); /* 默认使用AND连接 */
		return this.conditions;
	}
	
	@Override
	public ISqlQuery query() {
		if(this.empty == null) {
			this.empty = (SqlQuery) Query.to("1").eq("1");
			this.query(this.empty);
		}
		return this.empty;
	}
	
	@Override
	public ISqlQuery markOrderBy(String propertyName, boolean isAsc) {
		SqlQuery query = new SqlQuery(propertyName, 
				SqlQueryStrategy.ORDER_BY.instance(),
				Boolean.valueOf(isAsc));
		if(this.sorts == null) {
			this.sorts = new SqlQueryGroup(query);
			return query;
		}
		this.sorts.addQuery(query);
		return query;
	}
	
	@Override
	public ISqlQuery markSelect(String propertyName) {
		SqlQuery query = new SqlQuery(propertyName, 
				SqlQueryStrategy.SELECT.instance());
		if(this.selects == null) {
			this.selects = new SqlQueryGroup(query);
			return query;
		}
		this.selects.addQuery(query);
		return query;
	}

	@Override
	public ISqlQuery setPagination(int page, int count, int offset) {
		page = page < 1 ? 1 : page;
		count = count < 0 ? 10 : count;
		offset = offset < 0 ? 0 : offset;
				
		int start = (page - 1) * count + offset;
		this.limit = new SqlQuery(null,
				SqlQueryStrategy.LIMIT.instance(),
				Integer.valueOf(start), Integer.valueOf(count)); /* 只有一个LIMIT */
		return this.limit;
	}
	
	@Override
	public void deleteQuery(ISqlQuery query) {
		if(query == null) {
			throw new IllegalArgumentException("删除的查询条件不能为null");
		}
		SqlQuery queryReal = (SqlQuery) query;
		if(queryReal.belongGroup == null) {
			throw new IllegalArgumentException("删除的查询条件未加入此查询上下文");
		}
		queryReal.belongGroup.removeQuery(queryReal);
	}
	
	@Override
	public void deleteConditions() {
		this.conditions = null;
	}

	@Override
	public void deleteSorts() {
		this.sorts = null;
	}

	@Override
	public void deletePagination() {
		this.limit = null;
	}
	
	@Override
	public void clear() {
		this.conditions = null;
		this.limit = null;
		this.sorts = null;
	}
	
	@Override
	public ISqlMapContext generateMapContext(ISqlParameterContext paramContext, ISqlMapper sqlMapper) {
		if(this.empty != null) {
			this.deleteQuery(this.empty);
		}
		SqlMapContext mapContext = new SqlMapContext(paramContext, sqlMapper);
		SqlParameter.handleQuery(mapContext, this.selects, false);
		SqlParameter.handleQuery(mapContext, this.conditions, false);
		SqlParameter.handleQuery(mapContext, this.sorts, false);
		SqlParameter.handleQuery(mapContext, this.limit, false);
		return mapContext;
	}
	
	/**
	 * SQL成员的实现
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static class SqlPart implements ISqlPart {

		/**
		 * SQL成员类型字符串
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final String type;
		
		/**
		 * SQL内容
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final StringBuilder content;
		
		/**
		 * 特别指定的映射字符串, 一般为正则表达式, 最终作为映射键值对的值
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private String assignedMapStr;
		
		/**
		 * 使用的拼接处理器类型
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private ISqlJoinStrategy usingJoinStrategy;
		
		/**
		 * SQL中的参数对象数组
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private Object[] argObjs;
		
		/**
		 * 依赖的映射元信息的名称
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private Set<String> dependentMapMetaNames;
		
		/**
		 * 额外参数表
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private Map<String, Object> extras;
		
		/**
		 * 构造SQL成员实例, 默认
		 * 
		 * @param type 映射类型
		 * @param content SQL内容
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public SqlPart(String type, StringBuilder content) {
			this.type = type;
			this.content = content;
		}
		
		/**
		 * 构造SQL成员实例, 通过另一个SQL成员实例来实例化, 其中SQL内容为空.
		 * 
		 * @param sqlPart 另一个SQL成员实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public SqlPart(ISqlPart sqlPart) {
			SqlPart real = (SqlPart) sqlPart;
			this.type = real.type;
			this.usingJoinStrategy = real.usingJoinStrategy;
			this.assignedMapStr = real.assignedMapStr;
			this.dependentMapMetaNames = real.dependentMapMetaNames;
			this.extras = real.extras;
			
			this.content = new StringBuilder();
			this.argObjs = null;
			
			if(real.usingJoinStrategy == null) {
				this.content.append(real.content);
			}
		}
		
		/**
		 * 设置SQL中的参数对象数组
		 * 
		 * @param argObjs SQL中的参数对象数组, 可以为null
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public void setArgObjs(Object[] argObjs) {
			this.argObjs = argObjs;
		}
		
		@Override
		public String getType() {
			return this.type;
		}
		
		@Override
		public void setAssignedMapStr(String assignedMapStr) {
			this.assignedMapStr = assignedMapStr;
		}
		
		@Override
		public String getAssignedMapStr() {
			return this.assignedMapStr;
		}

		@Override
		public StringBuilder getContent() {
			return this.content;
		}
		
		@Override
		public Object[] getArgObjs() {
			return this.argObjs;
		}

		@Override
		public Set<String> getDependentMapMetaNames() {
			return this.dependentMapMetaNames;
		}

		@Override
		public void addDependentMapMetaName(String name) {
			if(this.dependentMapMetaNames == null) {
				this.dependentMapMetaNames = new LinkedHashSet<String>();
			}
			this.dependentMapMetaNames.add(name);
		}

		@Override
		public ISqlJoinStrategy getUsingJoinStrategy() {
			return this.usingJoinStrategy;
		}
		
		@Override
		public void setUsingJoinStrategy(ISqlJoinStrategy joinStrategy) {
			this.usingJoinStrategy = joinStrategy;
		}

		@Override
		public void putExtra(String name, Object obj) {
			if(this.extras == null) {
				this.extras = new HashMap<String, Object>();
			}
			this.extras.put(name, obj);
		}

		@Override
		public Object getExtra(String name) {
			if(this.extras == null) {
				return null;
			}
			return this.extras.get(name);
		}
	}

	/**
	 * SQL查询条件的实现
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static class SqlQuery implements ISqlQuery {

		/**
		 * 使用的连接逻辑关系是否为AND
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		SqlQueryRelation relation;
		
		/**
		 * 查询的属性名称
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final String propertyName;
		
		/**
		 * 查询参数的对象数组
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final Object[] queryArgs;
		
		/**
		 * 使用的查询处理策略
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final ISqlQueryStrategy usingQueryStrategy;
		
		/**
		 * 所属的查询组
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		private SqlQueryGroup belongGroup;
		
		
		/**
		 * 构建一个查询实例, 需指定查询处理策略和查询参数
		 * 
		 * @param usingQueryStrategy 查询处理策略, 不能为null
		 * @param queryArgs 查询参数, 可以为null
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private SqlQuery(String propertyName, ISqlQueryStrategy usingQueryStrategy, Object...queryArgs) {
			this.propertyName = propertyName;
			this.usingQueryStrategy = usingQueryStrategy;
			this.queryArgs = queryArgs;
		}
		
		@Override
		public ISqlQuery and(ISqlQuery query) {
			return this.joinQuery((SqlQuery) query, SqlQueryRelation.AND);
		}

		@Override
		public ISqlQuery or(ISqlQuery query) {
			return this.joinQuery((SqlQuery) query, SqlQueryRelation.OR);
		}

		@Override
		public SqlQueryRelation getRelation() {
			return this.relation;
		}

		@Override
		public String getPropertyName() {
			return this.propertyName;
		}

		@Override
		public ISqlQueryStrategy getUsingStrategy() {
			return this.usingQueryStrategy;
		}

		@Override
		public Object[] getQueryArgs() {
			return this.queryArgs;
		}
		
		/**
		 * 连接当前查询条件和指定查询条件
		 * 
		 * @param query 指定的查询条件
		 * @param relation 连接关系
		 * @return 连接关系的头处查询条件
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private ISqlQuery joinQuery(SqlQuery query, SqlQueryRelation relation) {
			// 修改关系
			query.relation = relation;
			// 如果本身不是查询组则整理成查询组
			if(this.belongGroup == null && ! (this instanceof SqlQueryGroup)) {
				this.belongGroup = new SqlQueryGroup(this);
				this.belongGroup.addQuery(query);
				return this.belongGroup;
			}
			// 否则找到根查询组来加入此查询
			SqlQuery query1 = this;
			while(query1.belongGroup != null) {
				query1 = query1.belongGroup;
			}
			((SqlQueryGroup) query1).addQuery(query);
			return query1;
		}
	}
	
	/**
	 * SQL查询条件组
	 * 
	 * @author linjie
	 * @since 1.0.1
	 */
	static class SqlQueryGroup extends SqlQuery implements ISqlQueryGroup {
		
		/**
		 * 包含的子查询条件
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		private final Set<ISqlQuery> groupQueries = new LinkedHashSet<ISqlQuery>();
		
		/**
		 * 组的头
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		private final SqlQuery head;
		
		/**
		 * 构造一个SQL查询条件组
		 * 
		 * @param query 组的头
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		private SqlQueryGroup(ISqlQuery query) {
			super(null, query.getUsingStrategy()); /* 处理策略与组的头相同 */
			SqlQuery queryReal = (SqlQuery) query;
			this.head = queryReal;
			this.head.relation = null; /* 组的头的连接关系被组夺取了 */
			this.addQuery1(queryReal);
		}
		
		/**
		 * 添加一个查询条件到当前组
		 * 
		 * @param query 添加的查询条件实例
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		@Override
		public boolean addQuery(ISqlQuery query) {
			return this.addQuery1((SqlQuery) query);
		}
		
		@Override
		public boolean removeQuery(ISqlQuery query) {
			if(! this.groupQueries.contains(query)) {
				return false;
			}
			this.groupQueries.remove((SqlQuery) query);
			return true;
		}

		@Override
		public Collection<ISqlQuery> getQueries() {
			return Collections.unmodifiableCollection(this.groupQueries);
		}
		
		/**
		 * 添加一个查询条件到当前组
		 * 
		 * @param query 添加的查询条件实例
		 * 
		 * @author linjie
		 * @since 1.0.1
		 */
		private boolean addQuery1(SqlQuery query) {
			if(this.groupQueries.contains(query)) {
				return false;
			}
			this.groupQueries.add(query);
			query.belongGroup = this;
			return true;
		}
	}
	
	/**
	 * SQL查询处理
	 * <br/>利用enum来单例, 同时减少类文件
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	enum SqlQueryStrategy {
		
		/**
		 * 等于条件的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		EQ(new ISqlQueryStrategy() {
			
			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" = ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 不等于的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		NOT_EQ(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" <> ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 在集合中的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		IN(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
					.append(" IN (")
					.append(SqlQueryStrategy.generateReplacementArgsPlaceHolderStr(query.getQueryArgs().length))
					.append(") ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 不在集合中的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		NOT_IN(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
					.append(" NOT IN (")
					.append(SqlQueryStrategy.generateReplacementArgsPlaceHolderStr(query.getQueryArgs().length))
					.append(") ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 模糊查询的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		LIKE(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" LIKE ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 在范围中的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		BETWEEN(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" BETWEEN ? AND ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 小于的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		LESS_THAN(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" < ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 不小于的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		NOT_LESS_THAN(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" >= ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 大于的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		GREATER_THAN(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" > ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 不大于的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		NOT_GREATER_THAN(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" <= ? ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 为NULL的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		IS_NULL(new ISqlQueryStrategy() {
			
			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" IS NULL ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		/**
		 * 不为NULL的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		IS_NOT_NULL(new ISqlQueryStrategy() {
			
			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()))
						.append(" IS NOT NULL ");
				SqlQueryStrategy.handleConditionsQuery(mapContext, query, sqlPiece, args);
			}
		}),
		
		/**
		 * 字段输出的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		SELECT(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()));
				SqlPart sqlPart = new SqlPart(SqlPartType.SELECT.name(), sqlPiece);
				sqlPart.setUsingJoinStrategy(SqlJoinStrategy.JOIN_SELECT.instance());
				sqlPart.setAssignedMapStr(SqlMapper.REGEXP_SELECT);
				mapContext.addSqlPart(sqlPart);
			}
		}),
		
		/**
		 * 排序的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		ORDER_BY(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				Boolean isAsc = (Boolean) query.getQueryArgs()[0];
				
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append(SqlQueryStrategy.generatePropertyPlaceholder(query.getPropertyName()));
				if(isAsc) {
					sqlPiece.append(" ASC ");
				} else {
					sqlPiece.append(" DESC ");
				}
				SqlPart sqlPart = new SqlPart(SqlPartType.ORDER_BY.name(), sqlPiece);
				sqlPart.setUsingJoinStrategy(SqlJoinStrategy.JOIN_ORDERBYS.instance());
				sqlPart.setAssignedMapStr(SqlMapper.REGEXP_ORDER_BY);
				mapContext.addSqlPart(sqlPart);
			}
		}),
		
		/**
		 * 分页的查询处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		LIMIT(new ISqlQueryStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlQuery query, Object...args) {
				Integer start = (Integer) query.getQueryArgs()[0];
				Integer limit = (Integer) query.getQueryArgs()[1];
				
				StringBuilder sqlPiece = new StringBuilder();
				sqlPiece.append("LIMIT ").append(start).append(",").append(limit);
				SqlPart sqlPart = new SqlPart(SqlPartType.LIMIT.name(), sqlPiece);
				sqlPart.setAssignedMapStr(SqlMapper.REGEXP_LIMIT);
				mapContext.addSqlPart(sqlPart);
			}
		});
		
		/**
		 * 查询处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final ISqlQueryStrategy strategy;
		
		/**
		 * 内部构造器: 提供查询处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private SqlQueryStrategy(ISqlQueryStrategy strategy) {
			this.strategy = strategy;
		}
		
		/**
		 * 获取查询处理器的实例
		 * 
		 * @return 查询处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public ISqlQueryStrategy instance() {
			return strategy;
		}
		
		/**
		 * 生成属性映射占位字符串
		 * 
		 * @param propertyName 属性名称
		 * @return 属性映射占位字符串
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static String generatePropertyPlaceholder(String propertyName) {
			return new StringBuilder("{#").append(propertyName).append("}").toString();
		}
		
		/**
		 * 处理条件类型的SQL成员
		 * 
		 * @param mapContext
		 * @param query SQL查询
		 * @param sqlPiece SQL片段
		 * @param args
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static void handleConditionsQuery(ISqlMapContext mapContext,
				ISqlQuery query, StringBuilder sqlPiece, Object...args) {
			boolean isNeedGroupFormat = true;
			if(args.length > 0) {
				isNeedGroupFormat = (boolean) args[0];
			}
			// 如果是成组的条件查询, 则递归处理
			if(query instanceof SqlQueryGroup) {
				SqlParameter.handleQuery(mapContext, query, isNeedGroupFormat);
				return;
			}
			// 判断是否包含引用占位符, 如果是则进行格式化
			int scanningAtIndex = -1;
			for(int i = 0; i < query.getQueryArgs().length; i ++) {
				final Object queryArg = query.getQueryArgs()[i];
				if(! (queryArg instanceof String) || ! ((String) queryArg).matches(SqlMapper.REGEXP_ALL)) {
					continue;
				}
				// 参数中的'?'替换为该占位字符串
				final String propertyPlaceholder = (String) queryArg;
				scanningAtIndex = sqlPiece.indexOf("?", scanningAtIndex);
				sqlPiece.replace(scanningAtIndex, scanningAtIndex + 1, propertyPlaceholder);
				scanningAtIndex += propertyPlaceholder.length();
				// 把此参数设置为null
				query.getQueryArgs()[i] = null;
			}
			// 否则直接添加查询条件SQL片段到映射上下文
			SqlPart sqlPart = new SqlPart(SqlPartType.WHERE.name(), sqlPiece);
			sqlPart.setUsingJoinStrategy(SqlJoinStrategy.JOIN_CONDITIONS.instance());
			sqlPart.setArgObjs(FormatUtils.removeNullInArray(query.getQueryArgs()));
			if(query.getRelation() != null) {
				sqlPart.putExtra("relation", query.getRelation());
			}
			mapContext.addSqlPart(sqlPart);
		}
		
		/**
		 * 生成指定数量的参数占位字符串
		 * 
		 * @param count 数量
		 * @return 指定数量的参数占位字符串, 例如: ?,?,?...
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static String generateReplacementArgsPlaceHolderStr(int count) {
			StringBuilder result = new StringBuilder("?");
			for(int i = 0; i < count - 1; i ++) {
				result.append(",").append("?");
			}
			return result.toString();
		}
	}
	
	/**
	 * 查询方法实现
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static class QueryMethod implements ISqlQueryMethod {
		
		/**
		 * 查询的属性名称
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final String propertyName;
		
		/**
		 * 构建一个查询方法实例
		 * 
		 * @param propertyName 查询的属性名称
		 * 
		 * @author linjie
		 * @since 1.0.0
		 * 
		 */
		public QueryMethod(String propertyName) {
			this.propertyName = propertyName;
		}
		
		@Override
		public ISqlQuery eq(Object target) {
			if(target == null) {
				throw new IllegalArgumentException("查询条件eq的值不能为null");
			}
			return this.getQueryInstance(SqlQueryStrategy.EQ, target);
		}
		
		@Override
		public ISqlQuery notEq(Object target) {
			if(target == null) {
				throw new IllegalArgumentException("查询条件notEq的值不能为null");
			}
			return this.getQueryInstance(SqlQueryStrategy.NOT_EQ, target);
		}
		
		@Override
		public ISqlQuery in(Collection<?> targets) {
			if(targets == null || FormatUtils.removeNullInCollection(targets).isEmpty()) {
				throw new IllegalArgumentException("查询条件in的值集合不能为null或空");
			}
			return this.getQueryInstance(SqlQueryStrategy.IN, targets.toArray());
		}
		
		@Override
		public ISqlQuery notIn(Collection<?> targets) {
			if(targets == null || FormatUtils.removeNullInCollection(targets).isEmpty()) {
				throw new IllegalArgumentException("查询条件notIn的值集合不能为null或空");
			}
			return this.getQueryInstance(SqlQueryStrategy.NOT_IN, targets.toArray());
		}
		
		@Override
		public ISqlQuery like(String target) {
			if(target == null || target.isEmpty()) {
				throw new IllegalArgumentException("查询条件like的值不能为null或空");
			}
			return this.getQueryInstance(SqlQueryStrategy.LIKE, target);
		}
		
		@Override
		public ISqlQuery between(Object from, Object to) {
			if(from == null) {
				throw new IllegalArgumentException("查询条件between的范围起点的值不能为null");
			}
			if(to == null) {
				throw new IllegalArgumentException("查询条件between的范围终点的值不能为null");
			}
			return this.getQueryInstance(SqlQueryStrategy.BETWEEN, from, to);
		}
		
		@Override
		public ISqlQuery lessThan(Object target) {
			if(target == null) {
				throw new IllegalArgumentException("查询条件lessThan的值不能为null");
			}
			return this.getQueryInstance(SqlQueryStrategy.LESS_THAN, target);
		}
		
		@Override
		public ISqlQuery notLessThan(Object target) {
			if(target == null) {
				throw new IllegalArgumentException("查询条件notLessThan的值不能为null");
			}
			return this.getQueryInstance(SqlQueryStrategy.NOT_LESS_THAN, target);
		}
		
		@Override
		public ISqlQuery greaterThan(Object target) {
			if(target == null) {
				throw new IllegalArgumentException("查询条件greaterThan的值不能为null");
			}
			return this.getQueryInstance(SqlQueryStrategy.GREATER_THAN, target);
		}
		
		@Override
		public ISqlQuery notGreaterThan(Object target) {
			if(target == null) {
				throw new IllegalArgumentException("查询条件notGreaterThan的值不能为null");
			}
			return this.getQueryInstance(SqlQueryStrategy.NOT_GREATER_THAN, target);
		}
		
		@Override
		public ISqlQuery isNull() {
			return this.getQueryInstance(SqlQueryStrategy.IS_NULL);
		}
		
		@Override
		public ISqlQuery isNotNull() {
			return this.getQueryInstance(SqlQueryStrategy.IS_NOT_NULL);
		}
		
		/**
		 * 生成查询条件实例
		 * 
		 * @param strategy 查询策略
		 * @param args 参数
		 * @return 查询条件实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private SqlQuery getQueryInstance(SqlQueryStrategy strategy, Object...args) {
			return new SqlQuery(this.propertyName, strategy.instance(), args);
		}
	}
	
	/**
	 * 处理条件类型的查询条件
	 * 
	 * @param mapContext 映射上下文
	 * @param query 查询条件
	 * @param isNeedGroupFormat 是否需要格式化组
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private static void handleQuery(ISqlMapContext mapContext,
			ISqlQuery query, boolean isNeedGroupFormat) {
		if(query == null) {
			return;
		}
		// 判断是否是成组查询, 若不是成组查询直接让查询策略处理为SQL内容
		if(! (query instanceof SqlQueryGroup)) {
			query.getUsingStrategy().handle(mapContext, query);
			return;
		}
		// 是成组查询
		SqlQueryGroup queryGroup = (SqlQueryGroup) query;
		Set<ISqlQuery> groupQueries = queryGroup.groupQueries;
		boolean isSingleInGroup = groupQueries.size() == 1;
		// 先加入 '('
		if(isNeedGroupFormat && ! isSingleInGroup) {
			SqlPart sqlPart = new SqlPart(SqlPartType.WHERE.name(), new StringBuilder("("));
			sqlPart.setUsingJoinStrategy(SqlJoinStrategy.JOIN_CONDITIONS.instance());
			sqlPart.putExtra("relation", queryGroup.getRelation());
			mapContext.addSqlPart(sqlPart);
		}
		// 处理同组的查询
		for(ISqlQuery groupQuery : groupQueries) {
			SqlParameter.handleQuery(mapContext, groupQuery, ! isSingleInGroup);
		}
		// 最后加入 ')'
		if(isNeedGroupFormat && ! isSingleInGroup) {
			SqlPart sqlPart = new SqlPart(SqlPartType.WHERE.name(), new StringBuilder(")"));
			sqlPart.setUsingJoinStrategy(SqlJoinStrategy.JOIN_CONDITIONS.instance());
			mapContext.addSqlPart(sqlPart);
		}
	}
}
