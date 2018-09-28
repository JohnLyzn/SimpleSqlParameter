package com.fy.sqlparam.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
		if(collection != null && !collection.isEmpty()) {
			collection.removeAll(Collections.singleton(null));
			return collection;
		}
		return Collections.emptyList();
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
				if(arr != null) {
					System.arraycopy(arr, 0, result, offset, arr.length);
					offset += arr.length;
				}
			}
			return result;
		}
		return null;
	}
	
	/**
	 * 将数组字符串"[a, b, c...]"格式化为字符串列表
	 * @param arrayStr 数组字符串
	 * @return 字符串列表
	 *
	 * @author linjie
	 * @since 1.0.0
	 */
	public static List<String> arrayStr2List(String arrayStr) {
		return FormatUtils.arrayStr2List(arrayStr, false);
	}
	
	/**
	 * 将数组字符串"[a, b, c...]"格式化为字符串列表
	 * @param arrayStr 数组字符串
	 * @param isWipeRepeat 是否去除相同的元素
	 * @return 字符串列表
	 *
	 * @author linjie
	 * @since 1.0.0
	 */
	public static List<String> arrayStr2List(String arrayStr, boolean isWipeRepeat) {
		if(arrayStr != null && !arrayStr.isEmpty()){
			arrayStr = formatArrayStr(arrayStr);
			String[] elements = arrayStr.split(",");
			List<String> res = new ArrayList<String>(elements.length);
			for(String element : elements) {
				if(isWipeRepeat && res.contains(element)) {
					continue;
				}
				res.add(element);
			}
			// 去掉null
			res.removeAll(Collections.singleton(null));
			return res;
		}
		return Collections.emptyList();
	}
	
	/**
	 * 格式化字符串数组的字符串, 去掉多余的空格,多余的逗号, 左中括号, 右中括号
	 * 
	 * @param wfArrayStr 格式不正确的字符串数组
	 * @return 格式化好的字符串数组的字符串
	 *
	 * @author linjie
	 * @since 1.0.0
	 */
	public static String formatArrayStr(String wfArrayStr) {
		if(wfArrayStr != null && !wfArrayStr.isEmpty()){
			// 去掉所有' ', '[', ']', '"'
			wfArrayStr = wfArrayStr.replaceAll("\"|\\u005B|\\u005D", "");
			// 连续的多个空格合并为一个
			wfArrayStr = wfArrayStr.replaceAll("[ ]{2,}", " ");
			// 去掉逗号和正文的前后的空格
			wfArrayStr = wfArrayStr.replaceAll("[ ]+,|,[ ]+", ",");
			// 去掉相邻的的逗号
			wfArrayStr = wfArrayStr.replaceAll(",{2,}|,$|^,", ",");
			// 去掉开头和结尾的逗号
			wfArrayStr = wfArrayStr.replaceAll(",$|^,", "");
		}
		return wfArrayStr;
	}
	
	/**
	 * 私有构造器, 禁止实例化此类
	 * 
	 * @author linjie
	 * @since 1.0.0
	 */
	private FormatUtils() {}
}
