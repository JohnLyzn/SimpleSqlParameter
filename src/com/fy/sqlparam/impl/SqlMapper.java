package com.fy.sqlparam.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fy.sqlparam.impl.SqlParameter.SqlPart;
import com.fy.sqlparam.map.ISqlMapContext;
import com.fy.sqlparam.map.ISqlMapEntry;
import com.fy.sqlparam.map.ISqlMapMeta;
import com.fy.sqlparam.map.ISqlMapStrategy;
import com.fy.sqlparam.map.ISqlMapper;
import com.fy.sqlparam.map.ISqlPart;

/**
 * SQL映射器实现
 * 
 * @author linjie
 * @since 1.0.0
 */
public class SqlMapper implements ISqlMapper {

	/**
	 * SQL中匹配额外关联表的映射位置的正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static final String REGEXP_EXTRA_TABLES = "\\{EXTRA_TABLES\\}";
	
	/**
	 * SQL中匹配条件的映射位置的正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static final String REGEXP_CONDITIONS = "\\{CONDITIONS\\}";
	
	/**
	 * SQL中匹配排序的映射位置的正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static final String REGEXP_ORDER_BY = "\\{ORDER_BY\\}";
	
	/**
	 * SQL中匹配分页的映射位置的正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static final String REGEXP_LIMIT = "\\{LIMIT\\}";
	
	/**
	 * SQL中匹配引用的映射位置的正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static final String REGEXP_REFERENCE = "\\{#:?\\w+?\\}";
	
	/**
	 * SQL中匹配判断是否有依赖的映射位置的正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static final String REGEXP_SWITCHDEPENDENT = "\\{.+(\\?(\\w+,?)+:.+)+\\}";
	
	/**
	 * SQL中匹配所有特殊字符串(被'{}'包围的字符串)的映射位置的正则表达式
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	static final String REGEXP_ALL = "\\{.*?\\}";

	@Override
	public void map(ISqlMapContext mapContext, ISqlPart sqlPart) {
		// 把待处理的SQL作无类型的SQL成员, 以处理其中的引用
		SqlMapStrategy.MAP_STATIC.instance().handle(mapContext, sqlPart);
		SqlMapStrategy.MAP_IFDEPENDENT.instance().handle(mapContext, sqlPart);
		SqlMapStrategy.MAP_REFRERENCE.instance().handle(mapContext, sqlPart);
	}
	
	/**
	 * 映射键值对的实现
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static class SqlMapEntry implements ISqlMapEntry {

		/**
		 * 映射键的字符串, 是正则表达式字符串
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final String mapStr;
		
		/**
		 * 包含映射的SQL内容的SQL成员
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final ISqlPart sqlPart;
		
		/**
		 * 构造SQL映射关系实例
		 * 
		 * @param mapStr 映射键的字符串, 是正则表达式字符串
		 * @param sqlPart 包含映射的SQL内容的SQL成员
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public SqlMapEntry(String mapStr, ISqlPart sqlPart) {
			if(mapStr == null) {
				throw new IllegalArgumentException("映射键值对中映射字符串(键)不能为null");
			}
			if(sqlPart == null) {
				throw new IllegalArgumentException("映射键值对中包含映射的SQL内容的SQL成员(值)不能为null");
			}
			this.mapStr = mapStr;
			this.sqlPart = sqlPart;
		}
		
		@Override
		public String getMapStr() {
			return this.mapStr;
		}

		@Override
		public ISqlPart getSqlPart() {
			return this.sqlPart;
		}

		@Override
		public boolean actMapping(StringBuilder rawSql) {
			return SqlMapper.findAndReplaceContentByRegExpStr(rawSql, 
					this.mapStr, this.sqlPart.getContent().toString());
		}
	}
	
	/**
	 * SQL映射处理
	 * <br/>利用enum来单例, 同时减少类文件
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public enum SqlMapStrategy {
		
		/**
		 * 静态占位符的映射处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		MAP_STATIC(new ISqlMapStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlPart sqlPart, Object...args) {
				String mapStr = sqlPart.getAssignedMapStr();
				if(mapStr != null) {
					mapContext.addMapEntry(new SqlMapEntry(mapStr, sqlPart));
				}
			}
		}),
		
		/**
		 * 引用占位符的映射处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		MAP_REFRERENCE(new ISqlMapStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlPart sqlPart, Object...args) {
				StringBuilder sql = sqlPart.getContent();
				Pattern pattern = Pattern.compile(SqlMapper.REGEXP_REFERENCE);
				Matcher matcher = pattern.matcher(sql);
				while(matcher.find()) {
					String refrenceStr = sql.substring(matcher.start(), matcher.end());
					String refrenceName = refrenceStr.replaceAll("[\\{\\#\\}]", "");
					if(refrenceName.startsWith(":")) { /* 加上:仅用来提醒需要加入依赖, 本身这个位置不需要被替换 */
						refrenceName = refrenceName.replaceAll(":", "");
					}
					ISqlMapMeta mapMeta = mapContext.notifyHandleDependentMapMeta(refrenceName);
					if(mapMeta != null) {
						for(ISqlPart includeSqlPart : mapMeta.getSqlParts()) {
							String mapStr = includeSqlPart.getAssignedMapStr();
							if(mapStr == null) {
								mapContext.addMapEntry(new SqlMapEntry(
										this.formatRefrenceNameAsMapStr(refrenceName),
										includeSqlPart));
								break;
							}
						}
					}
				}
			}
			
			/**
			 * 格式化引用名称为映射字符串
			 * 
			 * @param refrenceName 引用名称
			 * @return 映射字符串
			 * 
			 * @author linjie
			 * @since 1.0.0
			 */
			private String formatRefrenceNameAsMapStr(String refrenceName) {
				return new StringBuilder("\\{#").append(refrenceName).append("\\}").toString();
			}
		}),
		/**
		 * 引用占位符的映射处理方案
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		MAP_IFDEPENDENT(new ISqlMapStrategy() {

			@Override
			public void handle(ISqlMapContext mapContext, ISqlPart sqlPart, Object...args) {
				StringBuilder sql = sqlPart.getContent();
				Pattern pattern = Pattern.compile(SqlMapper.REGEXP_SWITCHDEPENDENT);
				Matcher matcher = pattern.matcher(sql);
				while(matcher.find()) {
					String switchesStr = sql.substring(matcher.start(), matcher.end());
					String[] switchStrs = switchesStr.replaceAll("\\{|\\}", "").split("\\?");
					String defaultStr = switchStrs[0];
					String choiceStr = null;
					for(int i = 1; i < switchStrs.length; i ++) {
						String[] conditionAndContent = switchStrs[i].split(":");
						String[] dependentNames = conditionAndContent[0].split(",");
						boolean hasHandleDependency = false;
						for(String dependentName : dependentNames) {
							if(mapContext.hasHandleDependentMapMeta(dependentName)) {
								hasHandleDependency = true;
								break;
							}
						}
						if(hasHandleDependency) {
							choiceStr = conditionAndContent[1];
							break;
						}
					}
					if(choiceStr == null) {
						choiceStr = defaultStr;
					}
					mapContext.addMapEntry(new SqlMapEntry(switchesStr, 
							new SqlPart(null, new StringBuilder(choiceStr))));
				}
			}
		});
	
		/**
		 * 映射处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private final ISqlMapStrategy strategy;
		
		/**
		 * 内部构造器: 提供映射处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		private SqlMapStrategy(ISqlMapStrategy strategy) {
			this.strategy = strategy;
		}
		
		/**
		 * 获取映射处理器的实例
		 * 
		 * @return 映射处理器的实例
		 * 
		 * @author linjie
		 * @since 1.0.0
		 */
		public ISqlMapStrategy instance() {
			return this.strategy;
		}
	}
	
	/**
	 * 当没有对应的映射键值对时删除正则表达式匹配的字符串
	 * 
	 * @param sql SQL语句
	 * @param regExpStr 目标正则表达式
	 * @param entries 映射键值列表
	 * @return SQL中是否找到了匹配的字符串, 是则返回<tt>true</tt>, 否则返回<tt>false</tt>
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static boolean wipeRegExpStrWhenNoMapEntries(StringBuilder sql,
			String regExpStr, List<ISqlMapEntry> entries) {
		if(entries == null || entries.isEmpty()) {
			SqlMapper.findAndReplaceContentByRegExpStr(sql, regExpStr, "");
			return true;
		}
		return false;
	}
	
	/**
	 * 在SQL中找到匹配正则表达式的内容并替换为目标内容
	 * 
	 * @param sql SQL语句
	 * @param regExpStr 正则表达式
	 * @param replaceBy 替换的内容
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	public static boolean findAndReplaceContentByRegExpStr(StringBuilder sql,
			String regExpStr, String replaceBy) {
		Pattern pattern = Pattern.compile(regExpStr);
		Matcher matcher = pattern.matcher(sql);
		String result = matcher.replaceAll(replaceBy);
		boolean hasReplaced = ! result.equals(sql.toString());
		if(hasReplaced) {
			sql.delete(0, sql.length());
			sql.append(result);
			return true;
		}
		return false;
	}
}
