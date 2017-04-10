package cn.edu.scnu.TempMT_Index.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author CXH 有效时间
 */
public class ValidTime implements Comparable<ValidTime> {

	private long left;// 开始时间
	private long right;// 结束时间

	public ValidTime() {
		// TODO Auto-generated constructor stub
	}

	public ValidTime(long left, long right) {
		this.left = left;
		this.right = right;
	}

	public ValidTime(String left, String right) {
		this.left = parse(left);
		this.right = parse(right);
	}

	/**
	 * 字符串时间解析成long型
	 * 
	 * @param strTime
	 * @return
	 */
	public static long parse(String strTime) {
		Long longTime = null;
		try {
			longTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strTime).getTime();
		} catch (ParseException e1) {
			try {
				longTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(strTime).getTime();
			} catch (ParseException e2) {
				try {
					longTime = new SimpleDateFormat("yyyy-MM-dd").parse(strTime).getTime();
				} catch (ParseException e3) {
					e1.printStackTrace();
					e2.printStackTrace();
					e3.printStackTrace();
				}

			}
		}
		return longTime;
	}

	public String getLeftString() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.getLeft()));
	}

	public void setLeftString(String left) {
		this.left = parse(left);
	}

	public String getRightString() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.getRight()));
	}

	public void setRightString(String right) {
		this.right = parse(right);
	}

	public long getLeft() {
		return left;
	}

	public void setLeft(long left) {
		this.left = left;
	}

	public long getRight() {
		return right;
	}

	public void setRight(long right) {
		this.right = right;
	}

	// 几种比较方法
	// this.left<o.left，则为-1，left大，则为1，否则为0。
	public int compareLeftTo(ValidTime o) {
		return this.left < o.left ? -1 : (this.left == o.left ? 0 : -1);
	}

	/**
	 * //返回-1，this在前或包含o；返回0，相等区间；返回1，this在后或o包含this
	 */
	@Override
	public int compareTo(ValidTime o) {
		if (this.left < o.left) {
			return -1;
		} else if (this.left > o.left) {
			return 1;
		} else {
			if (this.right == o.right)
				return 0;
			else if (this.right > o.right)
				return -1;
			else
				return 1;
		}
	}

	/**
	 * 返回true，this包含point；返回false，this不包含point；
	 * 
	 * @param point
	 * @return
	 */
	public boolean isContain(long point) {
		if (this.left - point <= 0 && (this.right - point) >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 逗号间隔开
	 */
	@Override
	public String toString() {
		return getLeftString() + "," + getRightString();
	}

	// 交集
	public static ValidTime intersect(ValidTime[] o) {
		Arrays.sort(o);// 按开始时间由小到大排
		ValidTime R = o[0];
		for (int i = 1; i < o.length; i++) {
			if (R.right < o[i].left)
				R = null;
			else {
				R.left = R.left > o[i].left ? R.left : o[i].left;
				R.right = R.right < o[i].right ? R.right : o[i].right;
			}
		}
		return R;
	}

	// 并集
	// 当Rie<R(i+1)s时，将Ri放入已排数组Q中，i++转Step3。
	// 否则，令R(i+1)s=Ris，i++转Step3。直到i=R.size()，转Step4。
	public static ValidTime[] union(ValidTime[] o) {
		Arrays.sort(o);// 按开始时间由小到大排
		List<ValidTime> R = new ArrayList<>();
		ValidTime T = o[0];
		for (int i = 1; i < o.length; i++) {
			if (T.right < o[i].left) {
				R.add(T);
				T = o[i];
			} else {
				T.left = T.left < o[i].left ? T.left : o[i].left;
				T.right = T.right > o[i].right ? T.right : o[i].right;
			}
		}
		R.add(T);
		return (ValidTime[]) R.toArray(new ValidTime[R.size()]);
	}

	public static void main(String[] args) {
		// ValidTime vt = new ValidTime(1234234, 1234234);
		// System.out.println(vt.toString());
		ValidTime[] vtList = { new ValidTime(1, 3), new ValidTime(2, 4), new ValidTime(2, 5), new ValidTime(0, 3),
				new ValidTime(7, 8), new ValidTime(2, 6) };
		// ValidTime R = intersect(vtList);
		// if (R != null)
		// System.out.println(R.getLeft() + "," + R.getRight());
		ValidTime[] Ru = union(vtList);
		for (int i = 0; i < Ru.length; i++) {
			System.out.println(Ru[i].getLeft() + "," + Ru[i].getRight());
		}
	}
}
