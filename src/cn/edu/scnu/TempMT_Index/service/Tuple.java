package cn.edu.scnu.TempMT_Index.service;

/**
 * @author CXH
 *
 */
public class Tuple implements Comparable<Tuple> {
	private NoTime nt;// 非时态元组
	private ValidTime vt;// 有效时间

	public Tuple() {
		// TODO Auto-generated constructor stub
	}
	// obj包含时态和非时态
	public Tuple(Object[] obj) {
		if (obj.length < 2) {
			return;
		}
		Object[] ntObj = new Object[obj.length - 2];
		Object[] vtObj = new Object[2];
		System.arraycopy(obj, 0, ntObj, 0, ntObj.length);
		System.arraycopy(obj, obj.length - 2, vtObj, 0, vtObj.length);
		nt = new NoTime(ntObj);
		vt = new ValidTime(vtObj[0] + "", vtObj[1] + "");
	}

	// vtObj时态和ntObj非时态分开
	public Tuple(Object[] ntObj, long[] vtObj) {
		if (ntObj.length < 0 || vtObj.length != 2) {
			return;
		}
		nt = new NoTime(ntObj);
		vt = new ValidTime(vtObj[0], vtObj[1]);
	}

	/**
	 * 获取元组
	 * 
	 * @param i
	 * @return
	 */
	public String get(int i) {
		if (i < 0 && i > nt.getObj().length + 1) {
			return null;
		} else if (i == nt.getObj().length + 1) {
			return vt.getRightString();
		} else if (i == nt.getObj().length) {
			return vt.getLeftString();
		} else {
			return nt.getObj()[i] + "";
		}
	}

	public long getLeft() {
		return vt.getLeft();
	}

	public long getRight() {
		return vt.getRight();
	}

	public NoTime getNt() {
		return nt;
	}

	public void setNt(NoTime nt) {
		this.nt = nt;
	}

	public ValidTime getVt() {
		return vt;
	}

	public void setVt(ValidTime vt) {
		this.vt = vt;
	}

	@Override
	public String toString() {
		return nt.toString() + vt.toString();
	}

	@Override
	public int compareTo(Tuple o) {
		return this.vt.compareTo(o.vt);
	}
}

/**
 * 元组的非时态部分
 * 
 * @author CXH
 *
 */
class NoTime implements Comparable<NoTime> {
	private Object[] obj;

	public NoTime(Object[] obj) {
		this.setObj(obj);
	}
	@Override
	public boolean equals(Object obj) {
		return this.compareTo((NoTime)obj)==0?true:false;
	}
	@Override
	public int hashCode() {
		String s="";
		for (int i = 0; i < obj.length; i++) {
			s += obj[i]+"";
		}
		char [] chars = s.toCharArray();
		int n =0;
		for (int i = 0; i < chars.length; i++) {
			n += chars[i];
		}
		return n;
	}
	/**
	 * 返回0，字符相同；
	 */
	@Override
	public int compareTo(NoTime noTime) {
		if (this.obj.length != noTime.obj.length) {
			return -1;
		} else {
			for (int i = 0; i < obj.length; i++) 
				if (("" + this.obj[i]).compareTo("" + noTime.obj[i]) != 0) return -1;	
		}
		return 0;
	}

	public Object[] getObj() {
		return obj;
	}

	public void setObj(Object[] obj) {
		this.obj = obj;
	}

	/**
	 * 逗号间隔开
	 */
	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i < obj.length; i++) {
			str += obj[i] + ",";
		}
		return str;
	}
}
