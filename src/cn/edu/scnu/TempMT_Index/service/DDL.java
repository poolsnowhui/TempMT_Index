package cn.edu.scnu.TempMT_Index.service;

import java.util.ArrayList;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;

/**
 * 
 * @author CXH
 * 
 */
public class DDL {
	public static final String create = "create";
	public static final String table = "table";
	public static final String as = "as";
	public static final String validtime = "validtime";
	public static final String datetime = "datetime";
	public static final String vte_timeDB = "vte_timeDB";
	public static final String vts_timeDB = "vts_timeDB";
	public static final String drop = "drop";
	private ArrayList<String> strarray;
	private String mysql;
	private boolean flagAsValidtime = false;// 是否有as validtime字段
	private ReMessage message;
	private String tablename = "";

	private boolean ischeck(int index, String str2) {
		return strarray.get(index).toLowerCase().matches(str2);// 忽略大小写匹配
	}

	public String translate(String atsql, String[] parameters) {

		Format format = new Format();
		strarray = format.format(atsql);
		boolean flagAdded = false;// 是否增加了有效时间起始字段
		for (int i = strarray.size() - 1; i > 0; i--) {
			if (i > 0 && ischeck(i - 1, as) && ischeck(i, validtime)) {
				flagAsValidtime = true;
			}
			if (ischeck(i, validtime)) {
				strarray.remove(i);
			}
			if (ischeck(i, as)) {
				strarray.remove(i);
			}
			if (flagAsValidtime == true && flagAdded == false && ischeck(i, ".*[)].*")) {
				flagAdded = true;
				strarray.add(i, datetime);
				strarray.add(i, vte_timeDB);
				strarray.add(i, ",");
				strarray.add(i, datetime);
				strarray.add(i, vts_timeDB);
				strarray.add(i, ",");
			}
		}
		mysql = "";// mysql初始化为空字符串就可以直接进行+运算，否则会多出null字段
		for (int i = 0; i < strarray.size(); i++) {
			mysql += strarray.get(i);
			mysql += " ";
		}
		System.out.println("mysql=" + mysql);
		try {
			DAOFactory.getInstance().execute(mysql);
		} catch (Exception e) {
			e.printStackTrace();// 开发阶段
			// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
			// 可以处理，也可以放弃处理
			throw new RuntimeException(e.getMessage());
		}
		message.setErrMessage("定义表结构" + tablename + "成功");
		return "1" + "," + "define table" + "," + "success";

	}

	public ReMessage getMessage() {
		return message;
	}

	public void setMessage(ReMessage message) {
		this.message = message;
	}

}
