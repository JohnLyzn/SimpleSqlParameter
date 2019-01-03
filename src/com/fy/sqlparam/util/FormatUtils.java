package com.fy.sqlparam.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * 格式化工具类
 *
 * @author linjie
 * @since 1.0.0
 */
public final class FormatUtils {
	
	/**
	 * 把集合中的null去除
	 * 
	 * @param collection 需要去除null的集合
	 *
	 * @author linjie
	 * @since 1.0.0
	 */
	public static Collection<?> removeNullInCollection(Collection<?> collection) {
		if(collection == null || collection.isEmpty()) {
			return Collections.emptyList();
		}
		collection.removeAll(Collections.singleton(null));
		return collection;
	}
	
	/**
	 * 把数组中的null去除
	 * 
	 * @param array 需要去除null的数组
	 *
	 * @author linjie
	 * @since 1.0.0
	 */
	public static Object[] removeNullInArray(Object[] array) {
		if(array == null || array.length == 0) {
			return array;
		}
		return removeNullInCollection(new ArrayList<Object>(Arrays.asList(array))).toArray();
	}
	
	/**
	 * 把多个数组合成一个
	 * 
	 * @param main 主数组, 如果为null返回null
	 * @param arrs 要拼接上去的其它数组, 如果没有会返回主数组
	 * @return 合并完成的数组
	 *
	 * @author linjie
	 * @since 1.0.0
	 */
	@SafeVarargs
	public static <T> T[] concatArrays(T[] main, T[]...arrs) {
		if(main != null) {
			if(arrs == null || arrs.length == 0) {
				return main;
			}
			// 计算总长度
			int totalLen = main.length;
			for(T[] arr : arrs) {
				if(arr != null) {
					totalLen += arr.length;
				}
			}
			// 拼接
			int offset = main.length;
			T[] result = Arrays.copyOf(main, totalLen);
			for(T[] arr : arrs) {
				if(arr == null) {
					continue;
				}
				System.arraycopy(arr, 0, result, offset, arr.length);
				offset += arr.length;
			}
			return result;
		}
		return null;
	}
	
	/**
	 * 私有构造器, 禁止实例化此类
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private FormatUtils() {}
}
