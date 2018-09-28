package com.fy.sqlparam.map.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;

import com.fy.sqlparam.impl.SqlMapContext.SqlJoinStrategy;
import com.fy.sqlparam.impl.SqlMapContext.SqlMapMeta;
import com.fy.sqlparam.impl.SqlMapContext.SqlPartType;
import com.fy.sqlparam.impl.SqlMapper.SqlMapEntry;
import com.fy.sqlparam.impl.SqlParameter.SqlPart;
import com.fy.sqlparam.param.ISqlParameterContext;

/**
 * 提供映射元信息的配置
 * 
 * @author linjie
 * @since 1.0.0
 */
@Documented
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapMetaConfig {
	
	/**
	 * 基本表的SQL语句片段, 包括FROM表和一定会关联查询的表
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	String baseTables();
	
	/**
	 * 包含的可查询字段
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	FieldMapMeta[] fields();
	
	/**
	 * 包含的关联表, 可以没有
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	TableMapMeta[] joinTables() default {};
	
	/**
	 * 包含的默认查询, 所有查询到会加上的查询条件
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	Query[] defaultQueries() default {};
	
	/**
	 * 辅助类: 通过该类可以初始化一个搜索参数上下文的默认加入内容
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static class MapMetaAnnoInitializor {
		
		/**
		 * 为一个搜索参数上下文实例初始化映射元信息和默认加入内容(SQL成员和映射键值对)
		 * 
		 * @param paramContext 搜索参数上下文实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public static void initFor(ISqlParameterContext paramContext) {
			for(Constructor<?> constructor : paramContext.getClass().getConstructors()) {
				MapMetaConfig configAnno = constructor.getAnnotation(MapMetaConfig.class);
				if(configAnno != null) {
					// 基本表信息的映射内容
					String baseTablesSql = configAnno.baseTables();
					SqlMapEntry mapEntry = MapMetaAnnoInitializor.generateMapEntry("\\{BASE_TABLES\\}", baseTablesSql);
					paramContext.addDefaultMapEntry(mapEntry);
					
					FieldMapMeta[] fieldAnnoes = configAnno.fields();
					TableMapMeta[] joinTableAnnoes = configAnno.joinTables();
					Query[] defaultQueryAnnoes = configAnno.defaultQueries();
					// 字段的映射内容
					for(FieldMapMeta fieldAnno : fieldAnnoes) {
						String fieldName = fieldAnno.name();
						String dbFieldName = fieldAnno.value();
						String[] dependentMapMetaNames = fieldAnno.dependencyNames();
						Query[] triggleQueryAnnoes = fieldAnno.triggleQueries();
						
						SqlMapMeta mapMeta = MapMetaAnnoInitializor.generateMapMeta(fieldName);
						SqlPart dbFieldSqlPart = MapMetaAnnoInitializor.generateSqlPart4DbField(fieldName,
								dbFieldName, dependentMapMetaNames);
						mapMeta.addSqlPart(dbFieldSqlPart);
						paramContext.addDefaultMapMeta(mapMeta);
						
						for(Query triggleQueryAnno : triggleQueryAnnoes) {
							String conditionSql = triggleQueryAnno.value();
							boolean isAnd = triggleQueryAnno.isAnd();
							String[] triggleDependentMapMetaNames = triggleQueryAnno.denpendencyNames();
							
							SqlPart sqlPart = MapMetaAnnoInitializor.generateSqlPart4QueryContent(
									conditionSql, isAnd, triggleDependentMapMetaNames);
							mapMeta.addSqlPart(sqlPart);
						}
					}
					// 关联表信息的映射内容
					for(TableMapMeta joinTableAnno : joinTableAnnoes) {
						String tableMetaName = joinTableAnno.name();
						String joinTableSql = joinTableAnno.value();
						String[] dependentMapMetaNames = joinTableAnno.dependencyNames();
						
						SqlMapMeta mapMeta = MapMetaAnnoInitializor.generateMapMeta(tableMetaName);
						mapMeta.addSqlPart(MapMetaAnnoInitializor.generateSqlPart4JoinTables(joinTableSql,
								dependentMapMetaNames));
						paramContext.addDefaultMapMeta(mapMeta);
					}
					// 默认就会搜索的内容
					for(Query defaultQueryAnno : defaultQueryAnnoes) {
						String conditionSql = defaultQueryAnno.value();
						boolean isAnd = defaultQueryAnno.isAnd();
						String[] defaultDependentMapMetaNames = defaultQueryAnno.denpendencyNames();
						
						SqlPart sqlPart = MapMetaAnnoInitializor.generateSqlPart4QueryContent(conditionSql,
								isAnd, defaultDependentMapMetaNames);
						paramContext.addDefaultSqlPart(sqlPart);
					}
					break;
				}
			}
		}
		
		/**
		 * 辅助方法: 生成一个映射元信息实例
		 * 
		 * @param name 映射元信息的名称
		 * @return 映射元信息实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static SqlMapMeta generateMapMeta(String name) {
			return new SqlMapMeta(name);
		}
		
		/**
		 * 生成一个引用类型的SQL映射键值对
		 * 
		 * @param key 键
		 * @param value 值
		 * @return SQL映射键值对实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static SqlMapEntry generateMapEntry(String key, String value) {
			return new SqlMapEntry(key, new SqlPart(null, new StringBuilder(value)));
		}
		
		/**
		 * 为数据库字段生成一个SQL成员, 并指定依赖的关联表的映射元信息名称
		 * 
		 * @param fieldName 字段名称, 是查询使用的属性名称
		 * @param dbFieldName 数据库字段名称, 对应的字段名称引用会替换为此值, 包括数据库表别名
		 * @param dependentMapMetaNames 依赖的关联表的映射元信息名称
		 * @return SQL成员实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static SqlPart generateSqlPart4DbField(String fieldName, String dbFieldName,
				String[] dependentMapMetaNames) {
			SqlPart result = new SqlPart(null, new StringBuilder(dbFieldName));
			if(dependentMapMetaNames != null && dependentMapMetaNames.length > 0) {
				for(String dependentMapMetaName : dependentMapMetaNames) {
					result.addDependentMapMetaName(dependentMapMetaName);
				}
			}
			return result;
		}
		
		/**
		 * 为关联表生成一个SQL成员, 并指定依赖的其它关联表的映射元信息名称
		 * 
		 * @param joinTableSql 关联表的SQL语句
		 * @param dependentMapMetaNames 依赖的其它关联表的映射元信息名称
		 * @return SQL成员实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static SqlPart generateSqlPart4JoinTables(String joinTableSql,
				String[] dependentMapMetaNames) {
			SqlPart result = new SqlPart(SqlPartType.FROM_TABLES.name(), new StringBuilder(joinTableSql));
			result.setUsingJoinStrategy(SqlJoinStrategy.JOIN_JOINTABLES.instance());
			if(dependentMapMetaNames != null && dependentMapMetaNames.length > 0) {
				for(String dependentMapMetaName : dependentMapMetaNames) {
					result.addDependentMapMetaName(dependentMapMetaName);
				}
			}
			return result;
		}
		
		/**
		 * 为查询条件生成一个SQL成员, 并指定依赖的关联表的映射元信息名称
		 * 
		 * @param conditionSql 查询条件的SQL内容
		 * @param isAnd 连接的逻辑关系是否为AND, <tt>true</tt>为AND, <tt>false为OR</tt>
		 * @param dependentMapMetaNames 依赖的关联表的映射元信息名称
		 * @return SQL成员实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private static SqlPart generateSqlPart4QueryContent(String conditionSql, boolean isAnd,
				String[] dependentMapMetaNames) {
			SqlPart result = new SqlPart(SqlPartType.WHERE.name(), new StringBuilder(conditionSql));
			result.setUsingJoinStrategy(SqlJoinStrategy.JOIN_CONDITIONS.instance());
			result.putExtra("isAnd", isAnd);
			if(dependentMapMetaNames != null && dependentMapMetaNames.length > 0) {
				for(String dependentMapMetaName : dependentMapMetaNames) {
					result.addDependentMapMetaName(dependentMapMetaName);
				}
			}
			return result;
		}
	}
}
