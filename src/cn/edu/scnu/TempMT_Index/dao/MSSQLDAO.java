/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:chixh
 * create date: 2017年7月7日 下午1:44:42
 */
package cn.edu.scnu.TempMT_Index.dao;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import cn.edu.scnu.TempMT_Index.service.CONTANT;

/**
 * @author chixh
 *
 */
public class MSSQLDAO implements IDAO {
	private static MSSQLDAO instance = new MSSQLDAO();
	private static Connection conn;

	private MSSQLDAO() {
		try {
			Properties pp = new Properties();
			InputStream fis = new FileInputStream(CONTANT.confPath + "\\" + DAOFactory.MSSQL + ".properties");
			pp.load(fis);
			String driver = pp.getProperty("driver");
			String username = pp.getProperty("username");
			String password = pp.getProperty("password");
			String url = pp.getProperty("url");
			String database = pp.getProperty("database");
			String parameter = pp.getProperty("parameter");

			Class.forName(driver);
			conn = DriverManager.getConnection(url + parameter + database, username, password);
		} catch (Exception e) {
			// 错误处理
			Logger.getLogger(this.getClass()).error(e);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.scnu.TempMT_Index.dao.IDAO#executeQuery(java.lang.String)
	 */
	@Override
	public List<Object[]> executeQuery(String sql) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			List<Object[]> result = new ArrayList<>();
			Object[] head = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				head[i] = rsmd.getColumnName(i + 1);
			}
			result.add(head);
			while (rs.next()) {
				Object[] objects = new Object[columnCount];
				for (int i = 0; i < columnCount; i++) {
					objects[i] = rs.getObject(i + 1);
				}
				result.add(objects);
			}
			return result;
		} catch (SQLException e) {
			// 错误处理
			Logger.getLogger(this.getClass()).error(e);
			throw e;
		} finally {
			// 关闭资源
			if (rs != null && !rs.isClosed())
				rs.close();
			if (ps != null && !ps.isClosed())
				ps.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.scnu.TempMT_Index.dao.IDAO#executeQuery(java.lang.String[])
	 */
	@Override
	public ResultSet executeQuery(String[] sql) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.scnu.TempMT_Index.dao.IDAO#execute(java.lang.String[])
	 */
	@Override
	public int execute(String... sql) throws SQLException {
		PreparedStatement ps = null;
		try {
			conn.setAutoCommit(false);
			for (int i = 0; i < sql.length; i++) {
				ps = conn.prepareStatement(sql[i]);
				ps.executeUpdate();
			}
			conn.commit();
			return 1;
		} catch (SQLException e) {
			conn.rollback();
			// 错误处理
			Logger.getLogger(this.getClass()).error(e);
			throw e;
		} finally {
			// 关闭资源
			if (ps != null && !ps.isClosed())
				ps.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.scnu.TempMT_Index.dao.IDAO#callPro(java.lang.String)
	 */
	@Override
	public boolean callPro(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.scnu.TempMT_Index.dao.IDAO#getConnection()
	 */
	@Override
	public Connection getConnection() {
		return conn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.scnu.TempMT_Index.dao.IDAO#closeIDAO()
	 */
	@Override
	public void closeIDAO() throws SQLException {
		conn.close();

	}

	/**
	 * @return
	 */
	public static IDAO getInstance() {
		return instance;
	}

}
