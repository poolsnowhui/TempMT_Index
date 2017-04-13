/**
 * 
 */
package cn.edu.scnu.TempMT_Index.service;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author CXH
 *
 */
public class CreateService {

	/**
	 * //对整张表建索引，并存磁盘
	 * 
	 * @return
	 */
	public static String createLOBIndex() {
		// 1.找表
		ShowService showService = new ShowService();
		ArrayList<String> tableName = showService.showTable();
		long expense = 0;
		long resultNum = 0;
		String message = "";
		// 2.建索引
		for (int i = 0; i < tableName.size(); i++) {
			LOB lobCreate = new LOB();
			lobCreate.setTableName(tableName.get(i));
			lobCreate.disk012Create();
			expense += lobCreate.getExpense();
			resultNum += lobCreate.getBranchCount();
		}
		// 3.构造json数据
		JSONObject jsonobj = new JSONObject();
		try {
			jsonobj.append("atsql", "CREATE TEMPINDEX cxhIndex");// 语句
			jsonobj.append("message", message);// 错误消息
			jsonobj.append("totalNum", resultNum);// 查询总数
			jsonobj.append("expense", expense);// 开销时间
			jsonobj.append("indexType", "索引放磁盘");// 索引类型
			jsonobj.append("tableContent", "");// 查询得到的内容
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("clstr=" + jsonobj.toString());
		return jsonobj.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createLOBIndex();// 创建所有索引
	}

}
