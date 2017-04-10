package cn.edu.scnu.TempMT_Index.service;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;

/**
 * 
 * @author CXH
 *
 */
public class DQL {
	public static final String select = "select";
	public static final String from = "from";
	public static final String where = "where";
	public static final String and = "and";
	public static final String validtime = "validtime";
	public static final String as = "as";
	public static final String period = "period";
	public static final String projection = "projection";
	public static final String snapshot = "snapshot";
	public static final String distinct = "distinct";
	public static final String interval = "interval";
	public static final String date = "date";
	public static final String vts_timeDB = "vts_timeDB";
	public static final String vte_timeDB = "vte_timeDB";
	public static final String timestampdiff = "timestampdiff";
	/**
	 * FRAC_SECOND。表示间隔是毫秒 SECOND。秒 MINUTE。分钟 HOUR。小时 DAY。天 WEEK。星期 MONTH。月
	 * QUARTER。季度 YEAR。年
	 */
	public static final String[] timeMeasure = { "frac_second", "second", "minute", "hour", "day", "week", "month",
			"quarter", "year" };
	public static final String[] comparisonOperator = { "=", ">", "<", "<=", ">=", "<>", "!=" };

	private ArrayList<String> strarray;

	private boolean haveValidtime;// 带有效时间的查询标志位
	private boolean havePeriod;// 有效时间期间
	private boolean haveInterval;// 有效时间跨度
	private boolean haveSnapshot;// 快照查询标志位
	private boolean haveProjection;// 投影查询标志位
	private boolean haveDelete;// 是否执行过删除字段
	private boolean haveAddValidtime;// 是否增加过vts和vte字段
	private boolean haveWhere;// 是否有where子句

	private String vtsdatetime;// 查询开始时间
	private String vtedatetime;// 查询结束时间
	private String mysql;// 转化后的mysql语句
	private String dqlResult;// 查询的结果
	private boolean isFromDisk;// 是否在从磁盘中索引查询
	private int strategy = 10;
	private int threadCount = Runtime.getRuntime().availableProcessors();// 当前机器线程数
	// 信息
	private String tableName;// 当前语句的表名
	private long expense;// 时间开销
	private int resultNum;// 结果个数
	private String indexType;// 索引类型：磁盘索引（二分）索引类型：磁盘索引（非二分）索引类型：内存索引（二分）索引类型：内存索引（非二分）
	private ReMessage reMessage = new ReMessage();// 提示信息

	public String translate(String atsql, String[] parameters) {
		// 1.产生sql
		reMessage.setAtsql(atsql);
		productMysql(atsql, parameters);
		// 2.mysql查
		if (haveValidtime) {
			if (haveProjection) {
				dqlResult = projectQuery(mysql, parameters);
				return dqlResult;
			} else if (haveSnapshot) {
				dqlResult = snapQuery(mysql, parameters);
				return dqlResult;
			} else if (havePeriod) {
				if ((strategy & 8) == 8)
					// 使用时态索引
					return period(vtsdatetime, vtedatetime, tableName, mysql, parameters);
				else {
					dqlResult = sqlToStrVT(mysql, parameters);
				}
			} else {// 跨度，连接等其他带有效时间情况（不用额外使用程序处理的情况）
				dqlResult = sqlToStrVT(mysql, parameters);// 带有效时间的查询
			}
		} else {
			// 语句里不带有效时间的情况
			dqlResult = sqlToStr(mysql, parameters);// 不带有效时间的查询
		}
		return dqlResult;

	}

	public String productMysql(String atsql, String[] parameters) {
		// 1.生成mysql
		Format format = new Format();
		strarray = format.format(atsql);
		haveDelete = false;// 是否执行过删除字段
		haveAddValidtime = false;// 是否增加过vts和vte字段
		haveWhere = false;// 是否有where子句
		havePeriod = false;// 期间
		vtsdatetime = "";
		vtedatetime = "";
		haveInterval = false;// 跨度
		String intervalTimeMeasure = "";
		String intervalComparisonOperator = "";
		String intervalTimeAmount = "";
		haveSnapshot = false;// 快照
		String snapShotTime = "now";
		haveProjection = false;// 投影
		// 2.遍历关键字和删除关键字
		// System.out.println("2.遍历关键字和删除关键字");
		for (int i = 0; i < strarray.size(); i++) {
			// System.out.println(strarray.get(i));
			if (ischeck(i, validtime)) {
				haveValidtime = true;
				strarray.remove(i);
			}
			// 2.1 投影
			if (haveValidtime && ischeck(i, projection)) {
				haveProjection = true;
				strarray.remove(i);
				// 做投影语句操作
				break;
			}
			// 2.2快照
			if (haveValidtime && ischeck(i, snapshot)) {
				haveSnapshot = true;
				strarray.remove(i);
				if (ischeck(i, select)) {
					snapShotTime = "now";
					// strarray.add(i + 1, distinct);
				}
				// 做快照语句
				break;
			}
			// 2.3删跨度ATSQL字段
			if (haveValidtime && ischeck(i, interval)) {
				haveInterval = true;
				strarray.remove(i);
				if (haveInterval == true && i < strarray.size() && ischeck(i, timeMeasure)) {
					intervalTimeMeasure = strarray.get(i);
					strarray.remove(i);
					// System.out.println("删除时间度量单位");
					// System.out.println(strarray.get(i));
					if (i < strarray.size() && ischeck(i, comparisonOperator)) {
						intervalComparisonOperator = strarray.get(i);
						strarray.remove(i);
						// System.out.println("删除比较运算符");
						if (i < strarray.size() && isNumber(strarray.get(i))) {
							intervalTimeAmount = strarray.get(i);
							strarray.remove(i);
							// System.out.println("删除时间数量");
							haveInterval = true;// 时间跨度查询生效
						}
					}
				}
			} // end 跨度

			// 2.4删期间ATSQL字段
			if (haveValidtime && ischeck(i, period)) {
				havePeriod = true;
				strarray.remove(i);
				if (havePeriod && !haveDelete && ischeck(i, ".*[\\[(].*")) {
					haveDelete = true;
					strarray.remove(i);
					// System.out.println("删除[(");
					if (ischeck(i, ".*" + date + ".*")) {
						strarray.remove(i);
						// System.out.println("删除date");
						vtsdatetime = strarray.get(i).replaceAll("[\"\']", "");
						strarray.remove(i);
						// System.out.println("删除\"\"");
					} else if (strarray.get(i).matches(".*now.*")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						vtsdatetime = sdf.format(new Date());
						// System.out.println("删除now");
						strarray.remove(i);
					} else {
						System.err.println("日期格式不对,无开始时间");
					}
					if (ischeck(i, ".*[-].*")) {
						strarray.remove(i);
						// System.out.println("删除-");
					} else {
						System.err.println("日期格式不对，无-");
					}
					if (ischeck(i, date)) {
						strarray.remove(i);
						// System.out.println("删除date");
						vtedatetime = strarray.get(i).replaceAll("[\"\']", "");
						strarray.remove(i);
						// System.out.println("删除\"");
					} else if (strarray.get(i).matches(".*now.*")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						vtedatetime = sdf.format(new Date());
						strarray.remove(i);
						// System.out.println("删除now");
					} else {
						System.err.println("日期格式不对，无结束时间");
					}
					if (haveDelete && strarray.get(i).matches(".*[\\])].*")) {
						strarray.remove(i);
					} else {
						System.err.println("少了日期区间如])" + "或者前面没删除");
					}
				}
			} // end期间
				// System.out.println("删除期间字段");
				// if (ischeck(i, select)) {
				// haveSelect = true;
				// }
				// 2.5 SFW字段搜寻
			if (ischeck(i, where)) {
				haveWhere = true;
			}
			if (i > 0 && ischeck(i - 1, from) && (ischeck(i + 1, where) || ischeck(i + 1, ";"))) {
				tableName = strarray.get(i);// 找出当前语句的表名
			}
			// System.out.println("当前查询的表名" + tableName);
		}
		// 3.增加字段
		if (strategy == 0) {
			if (haveInterval == true && haveProjection == false && haveSnapshot == false && haveAddValidtime == false
					&& ischeck(strarray.size() - 1, ".*;.*"))
				addInterval(intervalTimeAmount, intervalComparisonOperator, intervalTimeMeasure, strarray.size() - 1);
			if (havePeriod == true && haveProjection == false && haveSnapshot == false && haveAddValidtime == false
					&& ischeck(strarray.size() - 1, ".*;.*")) // 无snapshot，无projection，vtsvte字段从未增加过时，从;号开始增加字段
				addPeriod(vtsdatetime, vtedatetime, strarray.size() - 1);
			if (haveSnapshot == true && !haveProjection && !havePeriod && !haveAddValidtime
					&& ischeck(strarray.size() - 1, ";"))
				addSnapShot(snapShotTime, strarray.size() - 1);
		}
		// 4.取SQL结果
		mysql = "";// mysql初始化为空字符串就可以直接进行+运算，否则会多出null字段
		for (int i = 0; i < strarray.size(); i++)

		{
			mysql += strarray.get(i);
			mysql += " ";
		}
		reMessage.setSql(mysql);
		return mysql;
	}

	public String snapQuery(String mysql, String[] parameters) {
		// 1.sqltoList
		if (mysql == null)
			mysql = "select * from " + tableName;
		List<Object[]> list = null;
		try {
			list = DAOFactory.getInstance().executeQuery(mysql);
		} catch (Exception e) {
			e.printStackTrace();// 开发阶段
			// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
			// 可以处理，也可以放弃处理
			throw new RuntimeException(e.getMessage());
		}
		int columnCounts = list.get(0).length - 2;
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append(columnCounts);
		for (int i = 0; i < columnCounts; i++) {
			resultBuffer.append(",");
			resultBuffer.append(list.get(0)[i]);
		}
		// 2.去掉List的头
		List<NoTime> noHeadList = new ArrayList<>();
		for (int i = 1; i < list.size(); i++) {
			Object[] copyOf = Arrays.copyOf(list.get(i), list.get(i).length - 2);
			noHeadList.add(new NoTime(copyOf));
		}
		long after = System.currentTimeMillis();
		// 3.将相同的部分合并
		Set<NoTime> set = new HashSet<>();
		set.addAll(noHeadList);
		long now = System.currentTimeMillis();
		// 4.处理结果
		resultBuffer.append(",");
		for (Iterator<NoTime> it = set.iterator(); it.hasNext();) {
			resultBuffer.append(it.next().toString());
		}
		if (resultBuffer.charAt(resultBuffer.length() - 1) == ',')
			resultBuffer.deleteCharAt(resultBuffer.length() - 1);// 刪除最後一個逗號
		dqlResult = resultBuffer.toString();
		// reMessage.setAtsql(atsql);
		// reMessage.setErrMessage(errMessage);
		reMessage.setRecordNum(set.size() + "");
		reMessage.setSql(mysql);
		reMessage.setStragey("distinct非时态元组");
		reMessage.setTimeExpense((now - after) + "");
		reMessage.setType("快照查询");
		// System.out.println(reMessage.toString());
		reMessage.setTableContent(dqlResult);
		return dqlResult;
	}

	/**
	 * 投影查询 validtime select projection * from tableName;
	 * 
	 * @param parameters
	 * @return
	 */
	public String projectQuery(String mysql, String[] parameters) {
		// 1.sqltoList
		if (mysql == null)
			mysql = "select * from " + tableName;
		List<Object[]> list = null;
		try {
			list = DAOFactory.getInstance().executeQuery(mysql);
		} catch (Exception e) {
			e.printStackTrace();// 开发阶段
			// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
			// 可以处理，也可以放弃处理
			throw new RuntimeException(e.getMessage());
		}
		int columnCounts = list.get(0).length;
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append(columnCounts);
		for (int i = 0; i < columnCounts; i++) {
			resultBuffer.append(",");
			resultBuffer.append(list.get(0)[i]);
		}
		// 2.去掉List的头
		List<Tuple> noHeadList = new ArrayList<>();
		for (int i = 1; i < list.size(); i++) {
			noHeadList.add(new Tuple(list.get(i)));
		}
		long after = System.currentTimeMillis();
		// 3.找相同nt的字段，归为一组，
		// ArrayList<ArrayList<Tuple>> R = new ArrayList<>();
		List<ValidTime> simplevt = new ArrayList<>();// 相同nt字段
		Map<NoTime, List<ValidTime>> R2 = new HashMap<>();
		// 在alTuple中取一个，遍历simpleNtTuple
		for (int i = 0; i < noHeadList.size(); i++) {
			// 3.1有相同的NT，则加入
			if (R2.containsKey(noHeadList.get(i).getNt())) {
				R2.get(noHeadList.get(i).getNt()).add(noHeadList.get(i).getVt());
			} else {
				// 3.2否则新增一个key
				simplevt = new ArrayList<>();
				simplevt.add(noHeadList.get(i).getVt());
				R2.put(noHeadList.get(i).getNt(), simplevt);
			}
		}
		// 4.对nt字段相等的vt做并集计算。
		for (Iterator<Entry<NoTime, List<ValidTime>>> i = R2.entrySet().iterator(); i.hasNext();) {
			Map.Entry<NoTime, List<ValidTime>> entry = i.next();
			List<ValidTime> value = entry.getValue();

			ValidTime[] union = ValidTime.union(value.toArray(new ValidTime[value.size()]));

			List<ValidTime> asList = Arrays.asList(union);
			entry.setValue(asList);
		}
		long now = System.currentTimeMillis();

		// 5.处理结果
		ArrayList<Tuple> resultTuples = new ArrayList<>();
		for (Iterator<Entry<NoTime, List<ValidTime>>> i = R2.entrySet().iterator(); i.hasNext();) {
			Entry<NoTime, List<ValidTime>> entry = i.next();
			NoTime nt = entry.getKey();
			List<ValidTime> vts = entry.getValue();
			for (int j = 0; j < vts.size(); j++) {
				Tuple tuple = new Tuple();
				tuple.setNt(nt);
				tuple.setVt(vts.get(j));
				resultTuples.add(tuple);
			}
		}
		for (int i = 0; i < resultTuples.size(); i++) {
			resultBuffer.append(",");
			resultBuffer.append(resultTuples.get(i).toString());
		}
		dqlResult = resultBuffer.toString();
		// reMessage.setAtsql(atsql);
		// reMessage.setErrMessage(errMessage);
		reMessage.setRecordNum(resultTuples.size() + "");
		reMessage.setSql(mysql);
		reMessage.setStragey("对相同非时态元组的有效时间组做并集运算");
		reMessage.setTimeExpense((now - after) + "");
		reMessage.setType("时态投影");
		// System.out.println(reMessage.toString());
		reMessage.setTableContent(dqlResult);
		return dqlResult;
	}

	/**
	 * 使用lob索引，内存，非二分法
	 * 
	 * @param vtsdatetime
	 * @param vtedatetime
	 * @param tableName
	 * @param mysql
	 * @param parameters
	 * @return
	 */
	public String period(String vtsdatetime, String vtedatetime, String tableName, String sql, String... parameters) {
		// 1.
		String columnsSql = "SHOW FIELDS FROM " + tableName;
		List<Object[]> al = null;
		try {
			al = DAOFactory.getInstance().executeQuery(columnsSql);
		} catch (Exception e) {
			e.printStackTrace();// 开发阶段
			// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
			// 可以处理，也可以放弃处理
			throw new RuntimeException(e.getMessage());
		}
		// 2.进行lob时态选择
		LOB lobQuery;
		if (isFromDisk) {
			lobQuery = new LOB(strategy, sql, new ValidTime(vtsdatetime, vtedatetime), tableName, true, threadCount);
		} else {
			lobQuery = new LOB(strategy, sql, new ValidTime(vtsdatetime, vtedatetime), tableName, false, threadCount);
		}
		List<Tuple> alTuple = lobQuery.queryProcess();

		// 3.构造查询结果字符串
		dqlResult = String.valueOf(al.size() - 1);// 个数
		for (int i = 1; i < al.size(); i++) {
			dqlResult += ",";
			dqlResult += al.get(i)[0];// 列属性名
		}
		for (int i = 0; i < alTuple.size(); i++) {
			for (int j = 0; j < alTuple.get(i).getNt().getObj().length + 2; j++) {
				dqlResult += ",";
				dqlResult += alTuple.get(i).get(j);// 每一列的值
			}
		}
		// 4.设置输出信息
		// reMessage.setAtsql(atsql);
		// reMessage.setErrMessage(errMessage);
		reMessage.setRecordNum(lobQuery.getResultNum() + "");
		reMessage.setSql(mysql);
		reMessage.setStragey(lobQuery.getStrategy() + "");
		reMessage.setTimeExpense(lobQuery.getExpense() + "");
		reMessage.setType("时态选择-期间包含");
		// System.out.println(reMessage.toString());
		reMessage.setTableContent(dqlResult);
		return dqlResult;
	}

	/**
	 * 不带有效时间
	 * result={4,id,name,email,course_id,1,cxh,qazcxh@emai.com,100001,...}
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	private String sqlToStr(String sql, String... parameters) {
		long after = System.currentTimeMillis();
		if (sql == "")
			sql = "select * from " + tableName;
		List<Object[]> source=null;
		try {
			source = DAOFactory.getInstance().executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();// 开发阶段
			// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
			// 可以处理，也可以放弃处理
			throw new RuntimeException(e.getMessage());
		}
		StringBuffer result = new StringBuffer();
		if (source != null)
			result.append("" + (source.get(0).length - 2));
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < source.get(i).length - 2; j++) {
				result.append(",");
				result.append(source.get(i)[j]);
			}
		}
		// 4.设置输出信息
		// reMessage.setAtsql(atsql);
		// reMessage.setErrMessage(errMessage);
		reMessage.setRecordNum(source.size() - 1 + "");
		reMessage.setSql(mysql);
		reMessage.setStragey(getIndexType());
		reMessage.setTimeExpense((System.currentTimeMillis() - after) + "");
		reMessage.setType("其他");
		// System.out.println(reMessage.toString());
		reMessage.setTableContent(result.toString());
		return result.toString();
	}

	/**
	 * 带有效时间
	 * result={6,id,name,email,course_id,vts_timeDB,vte_timeDB,1,cxh,qazcxh@emai
	 * .com,100001,vts,vte,...}
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	private String sqlToStrVT(String sql, String... parameters) {
		long after = System.currentTimeMillis();
		if (sql == "")
			sql = "select * from " + tableName;
		List<Object[]> source =null;
		try {
			source = DAOFactory.getInstance().executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		StringBuffer result = new StringBuffer();
		if (source != null)
			result.append("" + source.get(0).length);
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < source.get(i).length; j++) {
				result.append(",");
				result.append(source.get(i)[j]);
			}
		}
		// 4.设置输出信息
		// reMessage.setAtsql(atsql);
		// reMessage.setErrMessage(errMessage);
		reMessage.setRecordNum(source.size() - 1 + "");
		reMessage.setSql(mysql);
		reMessage.setStragey("无");
		reMessage.setTimeExpense((System.currentTimeMillis() - after) + "");
		// reMessage.setType("");
		// System.out.println(reMessage.toString());
		reMessage.setTableContent(result.toString());
		return result.toString();
	}

	/**
	 * 在where后面增加有效时间区间字段 validtime period [date "2001-01-01" - date
	 * "2002-01-01"] select * from tableName; vts_timeDB <= 2001-01-01 and
	 * vte_timeDB >= 2002-01-01;
	 * 
	 * @param vtsdatetime
	 * @param vtedatetime
	 * @return
	 */
	private boolean addPeriod(String vtsdatetime, String vtedatetime, int i) {
		haveAddValidtime = true;
		strarray.add(i, "\'" + vtedatetime + "\'");
		strarray.add(i, ">=");
		strarray.add(i, vte_timeDB);
		strarray.add(i, and);
		strarray.add(i, "\'" + vtsdatetime + "\'");
		strarray.add(i, "<=");
		strarray.add(i, vts_timeDB);
		if (haveWhere == true) {
			strarray.add(i, and);
		} else {
			strarray.add(i, where);
		}
		return true;
	}

	/**
	 * 在where后面增加有效时间跨度判断字段 validtime select interval month > 20 * from
	 * tableName;
	 * 
	 * @param intervalTimeAmount
	 * @param intervalComparisonOperator
	 * @param intervalTimeMeasure
	 * @param i
	 * @return
	 */
	private boolean addInterval(String intervalTimeAmount, String intervalComparisonOperator,
			String intervalTimeMeasure, int i) {
		haveAddValidtime = true;
		// where timestampdiff ( year , vts_timeDB , vte_timeDB ) <=
		// intervalTimeAmount ;
		strarray.add(i, intervalTimeAmount);
		strarray.add(i, intervalComparisonOperator);
		strarray.add(i, ")");
		strarray.add(i, vte_timeDB);
		strarray.add(i, ",");
		strarray.add(i, vts_timeDB);
		strarray.add(i, ",");
		strarray.add(i, intervalTimeMeasure);
		strarray.add(i, "(");
		strarray.add(i, timestampdiff);
		if (haveWhere == true)
			strarray.add(i, and);
		else
			strarray.add(i, where);

		setIndexType("时态选择-跨度查询");
		return true;
	}

	private void addSnapShot(String snapShotTime, int i) {
		haveAddValidtime = true;
		// where vts_timeDB <= snapShotTime and snapShotTime <= vte_timeDB ;
		if (snapShotTime == "now") {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			snapShotTime = sdf.format(new Date());
		}
		strarray.add(i, vte_timeDB);
		strarray.add(i, "<=");
		strarray.add(i, "\'" + snapShotTime + "\'");
		strarray.add(i, and);
		strarray.add(i, "\'" + snapShotTime + "\'");
		strarray.add(i, "<=");
		strarray.add(i, vts_timeDB);
		if (haveWhere)
			strarray.add(i, and);
		else
			strarray.add(i, where);
	}

	private boolean ischeck(int index, String... str) {
		for (int i = 0; i < str.length; i++) {
			if (strarray.get(index).toLowerCase().matches(str[i])) {
				return true;
			}
		}
		return false;
	}

	public int getStrategy() {
		return strategy;
	}

	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	public void setFromDisk(boolean isFromDisk) {
		this.isFromDisk = isFromDisk;
	}

	public int getResultNum() {
		return resultNum;
	}

	public void setResultNum(int resultNum) {
		this.resultNum = resultNum;
	}

	public long getExpense() {
		return expense;
	}

	public void setExpense(long expense) {
		this.expense = expense;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	public ReMessage getReMessage() {
		return reMessage;
	}

	public void setReMessage(ReMessage reMessage) {
		this.reMessage = reMessage;
	}

	/**
	 * 判断字符串是否是整数
	 */
	public static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 判断字符串是否是浮点数
	 */
	public static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
			if (value.contains("."))
				return true;
			return false;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 判断字符串是否是数字
	 */
	public static boolean isNumber(String value) {
		return isInteger(value) || isDouble(value);
	}

	public static void main(String[] args) {
		DQL dql = new DQL();
		String result = dql.projectQuery("select * from student", null);
		System.out.println(result);
	}

}
