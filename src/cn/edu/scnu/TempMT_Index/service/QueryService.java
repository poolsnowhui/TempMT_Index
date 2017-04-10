package cn.edu.scnu.TempMT_Index.service;

import java.sql.SQLException;
import java.util.ArrayList;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;

public class QueryService {

	public String translate(String atsql, String lob, String isFromDisk, String[] parameters) {
		ReMessage reMessage = new ReMessage();
		String str = "";
		ArrayList<String> strarray;

		long startTime = System.currentTimeMillis();// 开始时间
		if (atsql == null) {
			System.err.println("atsql空串");
			return "";
		}
		// 1.格式化atsql
		Format format = new Format();
		strarray = format.format(atsql);

		// 2.atsql语句进行分类，并转译
		try {
			for (int i = 0; i < strarray.size(); i++) {
				if (strarray.get(i).equalsIgnoreCase(DQL.select)) {
					// 111,第一个1：lob；第二个1：磁盘；第三个1：二分法
					DQL dql = new DQL();
					dql.setStrategy(Integer.valueOf(lob));
					dql.setFromDisk(Integer.valueOf(isFromDisk) == 1 ? true : false);
					str = dql.translate(atsql, parameters);
					reMessage = dql.getReMessage();

				} else if (strarray.get(i).equalsIgnoreCase(DDL.create) || strarray.get(i).equalsIgnoreCase(DDL.drop)) {
					DDL ddl = new DDL();
					str = ddl.translate(atsql, parameters);

				} else if (strarray.get(i).equalsIgnoreCase(DML.insert)) {
					DML dml = new DML();
					str = dml.translate(atsql, parameters);
				}
			}
			if (str == "") {
				try {
					DAOFactory.getInstance().executeQuery(atsql);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			}
		} catch (Exception e) {
			reMessage.setErrMessage(e.getMessage());
		}

		long endTime = System.currentTimeMillis();// 结束时间
		long totalTime = endTime - startTime;
		System.out.println("总开销：" + totalTime + "ms");// 计算总开销

		// 3.构造json数据
		reMessage.setTableContent(str);
		System.out.println(reMessage.toString());
		System.out.println(ReMessage.toJson(reMessage));
		return ReMessage.toJson(reMessage);
	}

}
