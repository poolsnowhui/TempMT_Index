package cn.edu.scnu.TempMT_Index.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;

public class ShowService {

	// 返回整个数据库的信息
	public ArrayList<String> showDatabases() {

		String showDatabases = "show databases;";
		List<Object[]> databaseList = null;
		try {
			databaseList = DAOFactory.getInstance().executeQuery(showDatabases);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		ArrayList<String> arrayList = new ArrayList<String>();
		for (int i = 1; i < databaseList.size(); i++) {
			arrayList.add(databaseList.get(i)[1] + "");
		}
		// try {
		// for (int i = 0; rs.next(); i++) {
		// arrayList.add(i, rs.getString(1));
		// // System.out.println(i);
		// // System.out.println(arrayList.get(i));
		// }
		// } catch (SQLException e) {
		// e.printStackTrace();
		// throw new RuntimeException();
		// }
		return arrayList;
	}

	// 返回当前数据库所有表的信息
	public ArrayList<String> showTable() {
		String showTable = "show tables ;";
		List<Object[]> tableList = null;
		try {
			tableList = DAOFactory.getInstance().executeQuery(showTable);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		ArrayList<String> arrayList = new ArrayList<String>();
		for (int i = 1; i < tableList.size(); i++) {
			arrayList.add(tableList.get(i)[0] + "");
		}
		// try {
		// for (int i = 0; rs.next(); i++) {
		// arrayList.add(i, rs.getString(1));
		// // System.out.println(i);
		// // System.out.println(arrayList.get(i));
		// }
		// rs.close();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// throw new RuntimeException();
		// }
		return arrayList;
	}

	// 查出的表通过json返回
	public String tableToJson() {
		ArrayList<String> al = new ShowService().showTable();
		// 构造json数组
		JSONObject jsonobj = new JSONObject();
		try {
			jsonobj.append("tableName", al);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		System.out.println(jsonobj.toString());
		return jsonobj.toString();
	}

	public static void main(String[] args) {
	}

}
