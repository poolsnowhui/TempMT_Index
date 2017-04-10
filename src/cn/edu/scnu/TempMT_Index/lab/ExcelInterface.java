/**
 * 
 */
package cn.edu.scnu.TempMT_Index.lab;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CXH
 *
 */
public class ExcelInterface {
	public String atsql = "";
	public String indexType = "";
	public int totalNum = 0;
	public int resultNum = 0;
	public long expense = 0;

	public static void javaToExcel(List<List<ExcelInterface>> alalstu) throws IOException {
		// Student[][] stu = new Student[5][4];
		// 使用StringBuilder比String的效率比高，占用计算机资源没有这么多。
		// 最后输出前，转成字符串就可以了。
		StringBuilder excelXMLStringBuilder = new StringBuilder("");
		// 构造好excelXML的头
		excelXMLStringBuilder.append(
				"<?xml version=\"1.0\"?><?mso-application progid=\"Excel.Sheet\"?><Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\"><Worksheet ss:Name=\"LOB查询实验结果分析1204\"><Table>");
		// 输出好表头
		excelXMLStringBuilder.append("<Row>");
		for (int i = 0; i < alalstu.get(0).size(); i++) {
			excelXMLStringBuilder
					.append("<Cell><Data ss:Type=\"Number\">" + alalstu.get(0).get(i).totalNum + "</Data></Cell>");
		}
		excelXMLStringBuilder.append("</Row>");

		// excelXMLStringBuilder
		// .append("<Row><Cell><Data ss:Type=\"String\">开销</Data></Cell>"
		// + "<Cell><Data ss:Type=\"String\">姓名</Data></Cell>"
		// + "<Cell><Data ss:Type=\"String\">班级</Data></Cell></Row>");

		// 构造出每一行
		for (int i = 0; i < alalstu.size(); i++) {
			// 先构造出<Row>节点
			excelXMLStringBuilder.append("<Row>");
			excelXMLStringBuilder
					.append("<Cell><Data ss:Type=\"String\">" + alalstu.get(i).get(0).indexType + "</Data></Cell>");
			for (int j = 0; j < alalstu.get(i).size(); j++) {
				// 再不停地构造好每一个<Cell>节点
				excelXMLStringBuilder
						.append("<Cell><Data ss:Type=\"String\">" + alalstu.get(i).get(j).expense + "</Data></Cell>");
			}
			excelXMLStringBuilder.append("</Row>");
			// 先构造出<Row>节点

		}
		// 再构造好excelXML的尾
		excelXMLStringBuilder.append("</Table></Worksheet></Workbook>");
		// 最后把这个字符串打印到c:\学生表.xml就完事了
		// false代表覆盖输出，不是在此文件的末尾继续输出
		PrintWriter printwriter = new PrintWriter(new FileWriter("d:\\LOB查询实验结果分析1204.xml", false));
		// 输出前把excelXMLStringBuilder转化成字符串
		printwriter.print(excelXMLStringBuilder + "");
		// 清空输出缓冲区
		printwriter.flush();
		// 必须关闭文件输出流，Java才会在文件打印出字符串，也就是二进制流，
		printwriter.close();
	}

	public static void main(String args[]) throws IOException {
		// ArrayList<Student> studentArr = new ArrayList<Student>();
		List<List<ExcelInterface>> alalstu = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			List<ExcelInterface> alTemp = new ArrayList<>();
			for (int j = 0; j < 7; j++) {
				ExcelInterface temp = new ExcelInterface();
				temp.atsql = "atsql";
				temp.expense = i * 1000;
				temp.indexType = "索引" + i;
				temp.resultNum = 2 * i;
				temp.totalNum = 100 * j;
				alTemp.add(temp);
			}
			alalstu.add(alTemp);
		}
		// Student s1 = new Student();
		// s1.indexType = "1";
		// s1.totalNum = 1;
		// s1.expense = 102;
		// studentArr.add(s1);
		// Student s2 = new Student();
		// s2.indexType = "2";
		// s2.totalNum = 1;
		// s2.expense = 101;
		// studentArr.add(s2);
		// Student s3 = new Student();
		// s3.indexType = "3";
		// s3.totalNum = 1;
		// s3.expense = 103;
		// studentArr.add(s3);
		javaToExcel(alalstu);
		System.out.println("d:\\LOB查询实验结果分析1203.xml已生成，该xml是专门以excel打开的xml");
	}
}
