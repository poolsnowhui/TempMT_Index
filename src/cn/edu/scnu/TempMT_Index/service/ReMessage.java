/**
 * 
 */
package cn.edu.scnu.TempMT_Index.service;

import java.lang.reflect.Field;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author CXH
 * @category 当前ATSQL语句： 当前转化后的SQL语句： 当前错误信息： 当前记录数： 当前时间开销： 当前语句类型： 当前索引策略：
 *           reMessage.setAtsql(atsql); reMessage.setErrMessage(errMessage);
 *           reMessage.setRecordNum(recordNum); reMessage.setSql(sql);
 *           reMessage.setStragey(stragey);
 *           reMessage.setTableContent(tableContent);
 *           reMessage.setTimeExpense((now - after)+"");
 *           reMessage.setType(type);
 * 
 */
public class ReMessage {
	private String atsql = "";
	private String sql = "";
	private String errMessage = "";
	private String recordNum = "";
	private String timeExpense = "";
	private String type = "";
	private String stragey = "";
	private String tableContent = "";

	public static String toJson(ReMessage reMessage) {

		Class<?> c = null;
		try {
			c = Class.forName(ReMessage.class.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Field[] fields = c.getDeclaredFields();
		JSONObject jObject = new JSONObject();
		for (Field f : fields) {
			f.setAccessible(true);
			String key = f.getName();
			try {
				Object value = f.get(reMessage);
				if (value == null) {
					jObject.append(key, "");
				} else if (value.getClass().isArray()) {
					String[] arr = (String[]) value;
					String s = arr[0];
					for (int i = 1; i < arr.length; i++) {
						s += ",";
						s += arr[i];
					}
					jObject.append(key, s);
				} else {
					jObject.append(key, value);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return jObject.toString();

	}

	public String getAtsql() {
		return atsql;
	}

	public void setAtsql(String atsql) {
		this.atsql = atsql;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}

	public String getRecordNum() {
		return recordNum;
	}

	public void setRecordNum(String recordNum) {
		this.recordNum = recordNum;
	}

	public String getTimeExpense() {
		return timeExpense;
	}

	public void setTimeExpense(String timeExpense) {
		this.timeExpense = timeExpense;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStragey() {
		return stragey;
	}

	public void setStragey(String stragey) {
		this.stragey = stragey;
	}

	public String getTableContent() {
		return tableContent;
	}

	public void setTableContent(String tableContent) {
		this.tableContent = tableContent;
	}

	@Override
	public String toString() {
		return "ReMessage [atsql=" + atsql + ", sql=" + sql + ", errMessage=" + errMessage + ", recordNum=" + recordNum
				+ ", timeExpense=" + timeExpense + ", type=" + type + ", stragey=" + stragey + ", tableContent="
				+ tableContent + "]";
	}

}
