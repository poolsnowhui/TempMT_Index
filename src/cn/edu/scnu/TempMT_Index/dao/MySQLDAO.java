/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年3月29日 下午4:56:07
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
 * @author CXH
 *
 */
public class MySQLDAO implements IDAO {
	private static MySQLDAO instance = new MySQLDAO();
	private static Connection conn;

	private MySQLDAO() {
		try {
			Properties pp = new Properties();
			InputStream fis = new FileInputStream(CONTANT.confPath + "\\" + DAOFactory.MySQL + ".properties");
			pp.load(fis);
			String driver = pp.getProperty("driver");
			String username = pp.getProperty("username");
			String password = pp.getProperty("password");
			String url = pp.getProperty("url");
			String database = pp.getProperty("database");
			String parameter = pp.getProperty("parameter");
			Class.forName(driver);
			conn = DriverManager.getConnection(url + database + parameter, username, password);
		} catch (Exception e) {
			// 错误处理
			Logger.getLogger(MySQLDAO.class).error(e);
			e.printStackTrace();
		}
	}

	/**
	 * @param username
	 * @param password
	 */
	private MySQLDAO(String username, String password) {
		try {
			Properties pp = new Properties();
			InputStream fis = new FileInputStream(CONTANT.confPath + "\\" + DAOFactory.MySQL + ".properties");
			pp.load(fis);
			String driver = pp.getProperty("driver");
			String url = pp.getProperty("url");
			String database = pp.getProperty("database");
			String parameter = pp.getProperty("parameter");
			Class.forName(driver);
			conn = DriverManager.getConnection(url + database + parameter, username, password);
		} catch (Exception e) {
			// 错误处理
			Logger.getLogger(this.getClass()).error(e);
			throw new RuntimeException();
		}
	}

	public static MySQLDAO getInstance() {
		return instance;
	}

	public static MySQLDAO getInstance(String username, String password) {
		instance = new MySQLDAO(username, password);
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see demo.IDAO#executeQuery(java.lang.String)
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
			// 抛出异常，抛出运行异常，可以给调用该函数的函数一个选择
			e.printStackTrace();
			Logger.getLogger(this.getClass()).error(e);
			// 可以处理，也可以放弃处理
			throw new RuntimeException(e.getMessage());
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
	 * @see demo.IDAO#executeQuery(java.lang.String[])
	 */
	@Override
	public ResultSet executeQuery(String[] sql) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see demo.IDAO#execute(java.lang.String)
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
			throw new RuntimeException();
		} finally {
			// 关闭资源
			if (ps != null && !ps.isClosed())
				ps.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see demo.IDAO#callPro(java.lang.String)
	 */
	@Override
	public boolean callPro(String sql) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareCall(sql);
			return ps.execute();
		} catch (SQLException e) {
			conn.rollback();
			// 错误处理
			Logger.getLogger(this.getClass()).error(e);
			throw new RuntimeException();
		} finally {
			// 关闭资源
			if (ps != null && !ps.isClosed())
				ps.close();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see demo.IDAO#getConnection()
	 */
	@Override
	public Connection getConnection() {
		return conn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see demo.IDAO#closeIDAO()
	 */
	@Override
	public void closeIDAO() throws SQLException {
		conn.close();
	}

	public static void main(String[] args) {
		try {
			List<Object[]> executeQuery = MySQLDAO.getInstance().executeQuery("select * from student;");
			for (Object[] objects : executeQuery) {
				System.out.println(objects[1]);
			}
		} catch (SQLException e) {
			Logger.getLogger(new Object() {
				// 静态方法中获取当前类名
				public Class<?> getClassForStatic() {
					return this.getClass();
				}
			}.getClassForStatic()).error(e);
			throw new RuntimeException();
		}
	}
}
