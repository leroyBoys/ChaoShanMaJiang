package com.rafo.chess.utils;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSDataType;
import com.smartfoxserver.v2.entities.data.SFSDataWrapper;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;

import java.util.*;


/**
 * 随机工具类
 * 
 */
public class MathUtils {
	public static final float EPSILON = 0.00004f;// 再把误差调得大一点,现在这样,在150级时百万次检查大概会出现8次超出误差值

	/**
	 * 返回>=low, <=hi的整数随机数，均匀分布
	 * 
	 * @param low
	 * @param hi
	 * @return
	 */
	public static int random(int low, int hi) {
		return (int) (low + (hi - low + 0.9) * Math.random());
	}

	/**
	 * 返回>=low, <hi的浮点随机数，均匀分布
	 * 
	 * @param low
	 * @param hi
	 * @return
	 */
	public static float random(float low, float hi) {
		return (float) (low + (hi - low) * Math.random());
	}

	/**
	 * 非均匀分布的数组，返回命中数组元素的索引 全未命中返回-1
	 * 
	 * @param rateArray
	 *            数组中各元素的值为该元素被命中的权重
	 * @return 命中的数组元素的索引
	 */
	public static int random(float[] rateArray) {
		int[] rateArrayInt = new int[rateArray.length];
		for (int i = 0; i < rateArray.length; i++) {
			rateArrayInt[i] = (int) (rateArray[i] * 100);
		}
		return MathUtils.random(rateArrayInt);
	}

	/**
	 * 非均匀分布的数组，返回命中数组元素的索引 全未命中返回-1
	 * 
	 * @param rateArray
	 *            数组中各元素的值为该元素被命中的权重
	 * @return 命中的数组元素的索引
	 */
	public static int random(Integer[] rateArray) {
		int[] rateArrayInt = new int[rateArray.length];
		for (int i = 0; i < rateArray.length; i++) {
			rateArrayInt[i] = rateArray[i];
		}
		return MathUtils.random(rateArrayInt);
	}

	/**
	 * 非均匀分布的数组，返回命中数组元素的索引 全未命中返回-1
	 * 
	 * @param rateArray
	 *            数组中各元素的值为该元素被命中的权重
	 * @return 命中的数组元素的索引
	 */
	public static int random(int[] rateArray) {
		if (null == rateArray) {
			throw new IllegalArgumentException("The random array must not be null!");
		}
		int arrayLength = rateArray.length;
		if (arrayLength == 0) {
			throw new IllegalArgumentException("The random array's length must not be zero!");
		}
		// 依次累加的和
		int rateSum = 0;
		// 从头开始 依次累加之后的各个元素和 的临时数组
		int[] rateSumArray = new int[arrayLength];

		for (int i = 0; i < arrayLength; i++) {

			if (rateArray[i] < 0) {
				throw new IllegalArgumentException("The array's element must not be equal or greater than zero!");
			}
			rateSum += rateArray[i];
			rateSumArray[i] = rateSum;
		}
		if (rateSum <= 0) {
			// 所有概率都为零，必然没有选中的元素，返回无效索引:-1
			return -1;
		}

		int randomInt = MathUtils.random(1, rateSum);
		int bingoIndex = -1;
		for (int i = 0; i < arrayLength; i++) {
			if (randomInt <= rateSumArray[i]) {
				bingoIndex = i;
				break;
			}
		}
		if (bingoIndex == -1) {
			throw new IllegalStateException("Cannot find out bingo index!");
		}
		return bingoIndex;
	}

	/**
	 * 返回是否满足概率值。
	 * 
	 * @param shakeNum
	 *            float 概率值 0.0---1.0
	 * @return 比如某操作有２０％的概率，shakeNum=0.2 如果返回true表明概率满足。
	 */
	public static boolean shake(float shakeNum) {
		if (shakeNum >= 1) {
			return true;
		}
		if (shakeNum <= 0) {
			return false;
		}

		return Math.random() < shakeNum;
	}

	public static float roundUp(float a, int digits) {
		int n = (int) Math.pow(10, digits);
		return (float) (Math.ceil(a * n) / n);
	}

	public static double roundUp(double a, int digits) {
		int n = (int) Math.pow(10, digits);
		return Math.ceil(a * n) / n;
	}

	public static float roundDown(float a, int digits) {
		int n = (int) Math.pow(10, digits);
		return (float) Math.floor(a * n) / n;
	}

	public static double roundDown(double a, int digits) {
		int n = (int) Math.pow(10, digits);
		return Math.floor(a * n) / n;
	}

	/**
	 * 从一个枚举中随机一个值
	 * 
	 * @param enumClass
	 *            枚举类型
	 * @return 随机出的一个枚举值
	 */
	public static <T extends Enum<T>> T random(Class<T> enumClass) {
		T[] elements = enumClass.getEnumConstants();
		return elements[random(0, elements.length - 1)];
	}

	/**
	 * 注意如有需要，请使用@see {@link AIUtils#luckyDraw(float[])}
	 * 
	 * 抽奖 按照rateAry[i]要求的概率 返回 i； 计算物品必然掉落的情况 适用
	 * 
	 * @param rateAry
	 *            概率数组 要求 数组元素 和为1
	 * @return
	 */
	@Deprecated
	public static int luckyDraw(float[] rateAry) {
		if (rateAry == null) {
			return -1;// modified by sxf 090310
		}

		int[] balls = new int[100];
		int pt = 0;
		for (int i = 0; i < rateAry.length; i++) {
			int mulRate = (int) (rateAry[i] * 100);
			for (int j = 0; j < mulRate; j++) {
				try {
					balls[pt] = i;
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					return rateAry.length - 1;
				}
				pt++;
			}
		}
		return balls[random(0, 99)];
	}

	public static int parseInt(Object input, int defaultValue) {
		if (input == null)
			return defaultValue;
		try {
			return Integer.parseInt(input.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static int compareFloat(float f1, float f2) {
		float delta = f1 - f2;
		if (Math.abs(delta) > EPSILON) {
			if (delta > 0) {
				return 1; // f1> f2
			} else if (delta < 0) {
				return -1;// f1<f2
			}
		}
		return 0;// f1==f2
	}

	public static int compareToByDay(Calendar dayone, Calendar daytwo) {
		if (dayone.get(Calendar.YEAR) > daytwo.get(Calendar.YEAR)) {
			return 1;
		} else if (dayone.get(Calendar.YEAR) < daytwo.get(Calendar.YEAR)) {
			return -1;
		} else {
			if (dayone.get(Calendar.MONTH) > daytwo.get(Calendar.MONTH)) {
				return 1;
			} else if (dayone.get(Calendar.MONTH) < daytwo.get(Calendar.MONTH)) {
				return -1;
			} else {
				if (dayone.get(Calendar.DAY_OF_MONTH) > daytwo.get(Calendar.DAY_OF_MONTH)) {
					return 1;
				} else if (dayone.get(Calendar.DAY_OF_MONTH) < daytwo.get(Calendar.DAY_OF_MONTH)) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}

	/**
	 * 计算两个日期间相差的天数(按24小时算)
	 * 
	 * @param enddate
	 * @param begindate
	 * @return
	 */
	public static int getIntervalDays(Date enddate, Date begindate) {
		long millisecond = enddate.getTime() - begindate.getTime();
		int day = (int) (millisecond / 24l / 60l / 60l / 1000l);
		return day;
	}

	/**
	 * 计算两个日期间相差的天数(按24小时算)
	 * 
	 * @param enddate
	 * @param begindate
	 * @return
	 */
	public static int getIntervalDays(long enddate, long begindate) {
		long millisecond = enddate - begindate;
		int day = (int) (millisecond / 24l / 60l / 60l / 1000l);
		return day;
	}

	/**
	 * 计算两个日期间相差的分钟数
	 * 
	 * @param enddate
	 * @param begindate
	 * @return
	 */
	public static int getIntervalMinutes(Date enddate, Date begindate) {
		long millisecond = enddate.getTime() - begindate.getTime();
		int minute = (int) (millisecond / 60l / 1000l);
		return minute;
	}

	/**
	 * 计算两个日期间相差的分钟数
	 * 
	 * @param enddate
	 * @param begindate
	 * @return
	 */
	public static int getIntervalMinutes(long enddate, long begindate) {
		int minute = (int) ((Math.abs(enddate - begindate)) / 60l / 1000l);
		return minute;
	}

	/**
	 * 限置为 >=min <max的值
	 * 
	 * @param original
	 * @param min
	 * @param max
	 * @return
	 */
	public static int setBetween(int original, int min, int max) {
		if (original >= max) {
			original = max - 1;
		}
		if (original < min) {
			original = min;
		}
		return original;
	}

	/**
	 * 限置为 >=min <max的值
	 * 
	 * @param original
	 * @param min
	 * @param max
	 * @return
	 */
	public static long setBetween(long original, long min, long max) {
		if (original >= max) {
			original = max - 1;
		}
		if (original < min) {
			original = min;
		}
		return original;
	}

	/**
	 * @param ary1
	 * @param ary2
	 * @return ary1 >= ary2 true else false
	 */
	public static boolean compareArrays(int[] ary1, int[] ary2) {
		if (ary1 != null && ary2 != null) {
			if (ary1.length == ary2.length) {
				for (int i = 0; i < ary1.length; i++) {
					if (ary1[i] < ary2[i]) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static int float2Int(float f) {
		return (int) (f + 0.5f);
	}

	/**
	 * 获取两数相除的结果,精确到小数
	 * 
	 * @param num
	 * @param deno
	 * @return
	 */
	public static float doDiv(int numerator, int denominator) {
		if (denominator != 0) {
			return numerator / (denominator + 0.0f);
		}
		return 0f;
	}

	public static float doDiv(float numerator, float denominator) {
		if (denominator != 0) {
			return numerator / (denominator);
		}
		return 0f;
	}

	/**
	 * 两个正整数相加
	 * 
	 * @param n1
	 *            第一个参数
	 * @param n2
	 *            第二个参数
	 * @return 相加后的结果
	 * @exception IllegalArgumentException
	 *                ,如果n1或者n2有一个负数,则会抛出此异常;如果n1与n2相加后的结果是负数,即溢出了,也会抛出此异常
	 */
	public static int addPlusNumber(final int n1, final int n2) {
		if (n1 < 0 || n2 < 0) {
			throw new IllegalArgumentException("Both n1 and n2 must be plus,but n1=" + n1 + " and n2 =" + n2);
		}
		final int _sum = n1 + n2;
		if (_sum < 0) {
			throw new IllegalArgumentException("Add n1 and n2 must be plus,but n1+n2=" + _sum);
		}
		return _sum;
	}

	/**
	 * 比较两个flaot是否相等，用{@link Float#equals()}实现
	 * 
	 * @param floatA
	 * @param floatB
	 * @return
	 */
	public static boolean floatEquals(float floatA, float floatB) {
		return ((Float) floatA).equals(floatB);
	}

	/**
	 * 给定一系列事件的发生频率，以这个频率估计概率，随机选择一个事件发生
	 * 
	 * @param frequencies
	 *            发生事件的频率数组
	 * @param excludeIndexSet
	 *            忽略的事件索引集合，这些事件的频率将被忽略，随机结果也不会返回这些索引<br/>
	 *            如果没有忽略的事件，可传入null
	 * @return 发生的事件索引，即在frequencies中的索引；频率全部为0则返回-1，表示没有事件发生
	 */
	public static int randomSelectByFrequency(final int[] frequencies, BitSet excludeIndexSet) {
		if (frequencies == null) {
			throw new IllegalArgumentException("frequencies is null");
		}
		if (frequencies.length == 0) {
			return -1;
		}
		int fromIndex = 0;
		int toIndex = frequencies.length;
		int total = 0;
		for (int i = fromIndex; i < toIndex; i++) {
			if (excludeIndexSet != null && excludeIndexSet.get(i)) {
				continue;
			}
			if (frequencies[i] < 0) {
				// 非法频率数据
				throw new IllegalArgumentException("frequency must not be negative. freqencies:"
						+ Arrays.toString(frequencies));
			}
			total += frequencies[i];
		}
		if (total <= 0) {
			// 没有发生的事件
			return -1;
		}
		int randomNum = random(1, total), happenIndex = -1;
		int partSum = 0;
		for (int i = fromIndex; i < toIndex; i++) {
			if (excludeIndexSet != null && excludeIndexSet.get(i)) {
				continue;
			}
			partSum += frequencies[i];
			if (randomNum <= partSum) {
				happenIndex = i;
				break;
			}
		}
		return happenIndex;
	}

	/**
	 * 对一组数求平均，结果趋向于较大的数
	 * <p>
	 * 算法：例如有一组数为{ a, b, c }，那么平均数 n = (a^4 + b^4 + c^4) / (a^3 + b^3 + c^3)
	 * <p>
	 * <strong>要求给定的数全部大于0<br/>
	 * 计算过程使用int，三次方或者四次方后有可能会超出上限，所以不要传进太大的数</strong>
	 * 
	 * @param nums
	 *            一组数
	 * @return 平均数
	 */
	public static int getAverageTendToGreater(int[] nums) {
		if (nums == null || nums.length == 0) {
			throw new IllegalArgumentException("no value");
		}
		if (nums.length == 1) {
			return nums[0];
		}
		int total4 = 0, total3 = 0;
		for (int i = 0; i < nums.length; i++) {
			int num = nums[i], tmp = 0;
			if (num <= 0) {
				throw new IllegalArgumentException("num error");
			}
			tmp = num * num * num;
			total3 += tmp;
			tmp *= num;
			total4 += tmp;
		}
		return (int) Math.round((double) total4 / total3);
	}

	/**
	 * 从一个数组中随机一个元素
	 * 
	 * @param <T>
	 * @param array
	 *            一个数组
	 * @return 数组中的某个元素
	 */
	public static <T> T randomFromArray(T[] array) {
		int len = array.length;
		return array[random(0, len - 1)];
	}

	/**
	 * 从0到total里面取出count个不重复的数
	 * 
	 * @param total
	 * @param count
	 * @return
	 */
	public static int[] getRandomUniqueIndexs(int total, int count) {
		if (total < 0 || count <= 0) {
			return null;
		}
		if (total <= count) {
			int[] indexs = new int[total];
			for (int i = 0; i < total; i++) {
				indexs[i] = i;
			}
			return indexs;
		} else {
			List<Integer> indexList = new ArrayList<Integer>();
			for (int i = 0; i < total; i++) {
				indexList.add(i);
			}
			int[] indexs = new int[count];
			for (int i = 0; i < count; i++) {
				int index = MathUtils.random(0, indexList.size() - 1);
				indexs[i] = indexList.get(index);
				indexList.remove(index);
			}
			return indexs;
		}
	}

	/**
	 * 根据权重列表得到不重复的随机索引
	 * 
	 * @param weightList
	 *            权重数组
	 * @param count
	 *            需要的随机个数
	 * @return 当权重总和为0时返回空数组
	 */
	public static int[] getRandomUniqueIndex(List<Integer> weightList, int count) {
		if (weightList == null || count > weightList.size()) {
			return new int[0];
		}
		List<Integer> weights = new ArrayList<Integer>();
		for (Integer w : weightList) {
			weights.add(w);
		}
		int[] results = new int[count];
		List<Integer> tempResults = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			int index = random(weights.toArray(new Integer[0]));
			if (index == -1) {
				results = new int[0];
				break;
			}
			weights.remove(index);
			for (int j : tempResults) {
				if (j <= index) {
					index++;
				}
			}
			results[i] = index;
			tempResults.add(index);
			Collections.sort(tempResults);
		}
		return results;
	}

	/**
	 * 求两点之间的直线距离
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float getDistance(int x1, int y1, int x2, int y2) {
		float x = (float)Math.abs(x1 - x2);
		float y = (float)Math.abs(y1 - y2);
		return (float)Math.sqrt(x * x + y * y);
	}
	
	/**
	 * 求两点之间的直线距离
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float getDistanceFloat(float x1, float y1, float x2, float y2) {
		float x = Math.abs(x1 - x2);
		float y = Math.abs(y1 - y2);
		return (float)Math.sqrt(x * x + y * y);
	}

	public static int pow(int base, int n) {
		for(int i =0;i<n;i++)
			base*=base;
		return base;
	}
	/**
	 * 通过三角函数计算移动距离坐标
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @param dis
	 * @return
	 */
	public static float[] calculateMoveDistance(float ax,float ay,float bx,float by, float dis){
		float disA = MathUtils.getDistanceFloat(ax, ay, bx, by);
		float[] res = new float[2];
		if(disA==0){
			res[0] = ax;
			res[1] = ay;
			return res;
		}
		float sinA = (bx-ax)/disA;
		float cosA = (by-ay)/disA;
		
		float Zx = dis*sinA +ax;
		float Zy = dis*cosA +ay;
		
		res[0] = Zx;
		res[1] = Zy;
		return res;
	}

	public static void main(String[] args) {
		int cardNum = 23;
	}

	public static final String FormateStringForLua(SFSArray sfsObject){
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		Iterator<SFSDataWrapper> it = sfsObject.iterator();
		while (it.hasNext()){
			SFSDataWrapper iter = it.next();
			sb.append(formatForType(iter)).append(",");
		}
		sb.append("}");
		return sb.toString();
	}

	public static final String FormateStringForLua(SFSObject sfsObject){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(String key:sfsObject.getKeys()){
			sb.append(key).append("=");
			sb.append(formatForType(sfsObject.get(key))).append(",");
		}
		sb.append("}");
		return sb.toString();
	}

	private static String formatForType(SFSDataWrapper obj){
		StringBuilder sb = new StringBuilder();
		if(obj.getTypeId().name().endsWith("_ARRAY")){
			if(obj.getTypeId() == SFSDataType.SFS_ARRAY) {
				SFSArray sfsArray = (SFSArray) obj.getObject();
				Iterator<SFSDataWrapper> iterator = sfsArray.iterator();
				sb.append("{");
				while (iterator.hasNext()) {
					SFSDataWrapper it = iterator.next();
					sb.append(formatForType(it)).append(",");
				}
				sb.append("}");
				return sb.toString();
			}

			Collection collection = (Collection) obj.getObject();
			sb.append("{").append(StringUtils.join(collection, ",")).append("}");
		}else if(obj.getTypeId() == SFSDataType.SFS_OBJECT){
			SFSObject sfsObject = (SFSObject) obj.getObject();
			return FormateStringForLua(sfsObject);
		}else {
			sb.append(obj.getObject().toString());
		}

		return sb.toString();
	}
}
