package cn.edu.scnu.TempMT_Index.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;

public class DML {
	public static final String update = "update";
	public static final String insert = "insert";
	public static final String delete = "delete";
	public static final String into = "into";
	public static final String values = "values";
	private ArrayList<String> strarray;
	private String mysql;
	private ReMessage message;

	/**
	 * 忽略大小写匹配
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	private boolean ischeck(int index, String... str) {
		for (int i = 0; i < str.length; i++)
			if (strarray.get(index).toLowerCase().matches(str[i]))
				return true;
		return false;
	}

	public String translate(String atsql, String[] parameters) {
		Format format = new Format();
		strarray = format.format(atsql);
		boolean flagvalper = false;// 是否执行过删除字段
		boolean flagvtsvte = false;// 是否增加过vts和vte字段
		String vtsdatetime = "";// 语句中的开始有效时间
		String vtedatetime = "";// 语句中的结束有效时间
		String tablename = "";// 语句中的表名
		ArrayList<String> colunmnvalue = new ArrayList<String>();// 用于归并查询表中是否需要归并的列值
		for (int i = 0; i < strarray.size(); i++) {
			if (i - 2 > 0 && ischeck(i - 2, into) && ischeck(i, values)) {
				tablename = strarray.get(i - 1);
				System.out.println(tablename);
			}
			if (flagvalper == false) {
				flagvalper = true;
				if (strarray.get(i).toLowerCase().matches(".*validtime.*")) {
					strarray.remove(i);
					System.out.println("删除validtime");
				}
				if (strarray.get(i).toLowerCase().matches(".*period.*")) {
					strarray.remove(i);
					System.out.println("删除period");
				}
				if (ischeck(i, ".*[\\[(].*")) {
					strarray.remove(i);
					System.out.println("删除[(");
					if (ischeck(i, ".*date.*")) {
						strarray.remove(i);
						System.out.println("删除date");
						vtsdatetime = strarray.get(i).replaceAll("[\"\']", "");
						strarray.remove(i);
						System.out.println("删除\"\"");
					} else if (ischeck(i, ".*now.*")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						vtsdatetime = sdf.format(new Date());
						System.out.println("删除now");
						strarray.remove(i);
					} else {
						System.err.println("日期格式不对,无开始时间");
					}
					if (ischeck(i, ".*[-].*")) {
						strarray.remove(i);
						System.out.println("删除-");
					} else {
						System.err.println("日期格式不对，无-");
					}
					if (ischeck(i, ".*date.*")) {
						strarray.remove(i);
						System.out.println("删除date");
						vtedatetime = strarray.get(i).replaceAll("[\"\']", "");
						strarray.remove(i);
						System.out.println("删除\"");
					} else if (ischeck(i, ".*now.*")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						vtedatetime = sdf.format(new Date());
						strarray.remove(i);
						System.out.println("删除now");
					} else {
						System.err.println("日期格式不对，无结束时间");
					}
					if (flagvalper == true && ischeck(i, ".*[\\])].*")) {
						strarray.remove(i);
					} else {
						System.err.println("少了日期区间如[]()");
					}
				}
			}
			// 在insert语句中找出列值
			if (i - 1 > 0 && ischeck(i - 1, values) && ischeck(i, "[(]")) {
				for (int columnvalueindex = 2; !ischeck(i - 2 + columnvalueindex, "[;]")
						&& !ischeck(i - 1 + columnvalueindex, "[)]"); columnvalueindex++) {
					if (ischeck(i - 1 + columnvalueindex, "[,]")) {
						continue;
					} else {
						colunmnvalue.add(strarray.get(i - 1 + columnvalueindex));
						System.out.println(strarray.get(i - 1 + columnvalueindex));
					}
				}
			}
			if (flagvtsvte == false && ischeck(i, ".*[)].*") && ischeck(i + 1, ".*;.*")) {
				flagvtsvte = true;
				strarray.add(i, "\'" + vtedatetime + "\'");
				strarray.add(i, ",");
				strarray.add(i, "\'" + vtsdatetime + "\'");
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
		message.setErrMessage("插入数据成功");
		return "1" + "," + "insert data" + "," + "success";
	}


	/**
	 * @return
	 */
	public ReMessage getMessage() {
		return message;
	}
	public static void main(String[] args) {
		DML dml = new DML();
		String sql = dml.translate(
				"validtime period [date '2014-09-01'- date '2017-07-01'] insert into student values('100','cxh','qazcxh@163.com')",
				null);
		System.out.println(sql);
	}

}
