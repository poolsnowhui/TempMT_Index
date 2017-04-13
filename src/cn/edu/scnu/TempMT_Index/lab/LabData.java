package cn.edu.scnu.TempMT_Index.lab;

import java.util.List;

import cn.edu.scnu.TempMT_Index.service.DDL;
import cn.edu.scnu.TempMT_Index.service.DML;
import cn.edu.scnu.TempMT_Index.service.DQL;
import cn.edu.scnu.TempMT_Index.service.Format;
import cn.edu.scnu.TempMT_Index.service.LOB;

public class LabData {

	private String str = "";
	private List<String> strarray;

	public ExcelInterface translate(String atsql, String lob, String isFromDisk, String[] parameters) {
		int resultNum = 0;// 总数
		long expense;// 开销
		String indexType = "";// 索引类型
		String message = "";

		long startTime = System.currentTimeMillis();// 开始时间

		if (atsql == null) {
			System.err.println("atsql空串");
			return null;
		}
		// 1.格式化atsql
		Format format = new Format();
		strarray = format.format(atsql);

		// 2.atsql语句进行分类，并转译
		for (int i = 0; i < strarray.size(); i++) {
			if (strarray.get(i).equalsIgnoreCase(DQL.select)) {
				DQL dql = new DQL();
				dql.setStrategy(Integer.valueOf(lob));
				dql.setFromDisk(Integer.valueOf(isFromDisk) == 1 ? true : false);

				try {
					setStr(dql.translate(atsql, parameters));
					indexType = dql.getIndexType();// 索引类型
				} catch (Exception e) {
					message = e.getMessage();
					// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
					// 可以处理，也可以放弃处理
				}
				resultNum = dql.getResultNum();
				expense = dql.getExpense();
			} else if (strarray.get(i).equalsIgnoreCase(DDL.create) || strarray.get(i).equalsIgnoreCase(DDL.drop)) {
				DDL ddl = new DDL();
				try {
					setStr(ddl.translate(atsql, parameters));
					message = "" + ddl.getMessage();
				} catch (Exception e) {
					message = e.getMessage();
					// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
					// 可以处理，也可以放弃处理
				}
			} else if (strarray.get(i).equalsIgnoreCase(DML.insert)) {
				DML dml = new DML();
				try {
					setStr(dml.translate(atsql, parameters));

				} catch (Exception e) {
					message = e.getMessage();
					// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
					// 可以处理，也可以放弃处理
				}
			}
		}
		// System.out.println("sql="+sql);
		long endTime = System.currentTimeMillis();// 结束时间
		long totalTime = endTime - startTime;
		expense = totalTime;// 计算总开销

		// 3.构造表的数据
		ExcelInterface st = new ExcelInterface();// 表数据
		st.atsql = atsql;// 语句
		st.resultNum = resultNum;// 查询结果集总数
		st.expense = expense;// 开销时间
		st.indexType = indexType;// 索引类型
		System.out.println(message);

		return st;
	}

	public static void main(String[] args) {
		LOB disk2 = new LOB();
		disk2.setTableName("student1000k");
		disk2.disk012Create2();
		disk2.setTableName("student2000k");
		disk2.disk012Create2();
		disk2.setTableName("student3000k");
		disk2.disk012Create2();
		disk2.setTableName("student4000k");
		disk2.disk012Create2();
		disk2.setTableName("student5000k");
		disk2.disk012Create2();
		// List<Tuple> result = disk2.queryFromDisk(new
		// ValidTime(1075716136000l, 1081059508000l));
		// for (int i = 0; i < result.size(); i++) {
		// System.out.println(result.get(i).toString());
		// }
		// long time = disk2.getTime()[3] + disk2.getTime()[4];
		// System.out.println("所用开销:" + time + "ms");
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
}
