package cn.edu.scnu.TempMT_Index.lab;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;
import cn.edu.scnu.TempMT_Index.dao.IDAO;
import cn.edu.scnu.TempMT_Index.service.Tuple;

public class ImportAndExportDataBase {
	private String tableName = "student";// 设置表名
	private int dataCount = 200000;// 设置表记录数

	public ArrayList<Tuple> ExportD() {
		String sql = "select * from " + tableName + dataCount / 1000 + "k;";
		// 1.SQL全表查
		IDAO sqlHelper = DAOFactory.getInstance();
		List<Object[]> queryR;
		try {
			queryR = sqlHelper.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		// 2.格式化Tuple
		ArrayList<Tuple> alTuple = new ArrayList<>();
		for (int i = 1; i < queryR.size(); i++) {
			alTuple.add(new Tuple(queryR.get(i)));
		}
		// 3.返回
		return alTuple;
	}

	public void ImportD() {
		// 2.创建表
		String sql = "create table " + tableName + dataCount / 1000 + "k";
		sql += " (sid int ," + "name varchar(255) ," + "e_mail varchar(255) ," + "course_id varchar(255), "
				+ "vts_timeDB timestamp, " + "vte_timeDB timestamp" + ")  ";
		sql += ";";// mysql用
		IDAO sqlHelper = DAOFactory.getInstance();
		try {
			System.out.println(sql);
			sqlHelper.execute(sql);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

		// 3.生成数据
		ProductData pd = new ProductData();
		pd.setDataCount(dataCount);
		// long startTime = System.currentTimeMillis();
		ArrayList<Tuple> alTuple = pd.getAllTuple();
		// System.out.println(""+(System.currentTimeMillis()-startTime)+"ms");

		// 4.插入数据
		int k = 0;
		int trCount = 500;// 每个事务语句数量
		String[] iSql = new String[trCount];
		for (int i = 0; i < alTuple.size(); i++) {

			String insertSql = " insert into ";
			insertSql += tableName + dataCount / 1000 + "k";
			insertSql += " values ( ";
			for (int j = 0; j < pd.getNoTimeCount(); j++) {
				insertSql += "\'";
				insertSql += alTuple.get(i).get(j);
				insertSql += "\',";
			}
			insertSql += "'";
			insertSql += alTuple.get(i).get(pd.getNoTimeCount());
			insertSql += "','";
			insertSql += alTuple.get(i).get(pd.getNoTimeCount() + 1);
			insertSql += "' ) ;";// sql结尾
			// System.out.println(insertSql);
			k = i % trCount;
			if (k != (trCount - 1)) {
				iSql[k] = insertSql;
			} else {
				iSql[k] = insertSql;
				try {
					System.out.println(insertSql);
					sqlHelper.execute(iSql);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
				// System.out.println("*******************************************");
			}
		}
		if (alTuple.size() % trCount != 0) {
			try {
				sqlHelper.execute(iSql);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}

	}

	public String getTableName() {
		return tableName;
	}

	public int getDataCount() {
		return dataCount;
	}

	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}

	public static void main(String[] args) {
//		int c = 20000;
//		for (int i = 10; i < 50; i++) {
//			ImportAndExportDataBase idb = new ImportAndExportDataBase();
//			System.out.println("生成sql：" + (i * c + c));
//			idb.setDataCount(i * c + c);
//			idb.ImportD();
//		}
		ImportAndExportDataBase idb = new ImportAndExportDataBase();
		System.out.println("生成sql：" + 5000000);
		idb.setDataCount(5000000);
		idb.ImportD();
//		System.out.println("生成sql：" + 6000000);
//		idb.setDataCount(6000000);
//		idb.ImportD();
	}
}
