package cn.edu.scnu.TempMT_Index.lab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.scnu.TempMT_Index.service.DDL;
import cn.edu.scnu.TempMT_Index.service.DML;
import cn.edu.scnu.TempMT_Index.service.DQL;
import cn.edu.scnu.TempMT_Index.service.Format;

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

		String tableName = "stu_table";
		String atsql = "validtime period [date '2000-06-01' - date '2008-01-11' ] select * from " + tableName;
		Integer[] dataNum = new Integer[] { 2, 5, 15, 20, 30, 40, 45, 50 };
		String[] lob = new String[] { "0", "4", "5", "6", "7", };
		LabData labData = new LabData();
		String[] parameters = null;
		List<List<ExcelInterface>> alalstu = new ArrayList<>();
		for (int i = 0; i < lob.length; i++) {
			List<ExcelInterface> alTemp = new ArrayList<>();
			for (int j = 0; j < dataNum.length; j++) {
				ExcelInterface temp = new ExcelInterface();
				long avg = 0;
				for (int k = 0; k < 5; k++) {
					temp = labData.translate(atsql + dataNum[j] + "k;", "0", lob[i], parameters);
					avg += temp.expense;
				}
				temp.expense = avg / 5;
				System.out.println(temp.expense);
				alTemp.add(temp);
			}
			alalstu.add(alTemp);
		}
		try {
			ExcelInterface.javaToExcel(alalstu);
		} catch (IOException e) {
			e.printStackTrace();// 开发用
			throw new RuntimeException(e.getMessage());// 上线用
		}
		System.out.println("d:\\LOB查询实验结果分析1204.xml已生成，该xml是专门以excel打开的xml");
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
}
