/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年3月29日 下午4:56:07
 */
package cn.edu.scnu.TempMT_Index.dao;

import java.io.File;
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

/**
 * @author CXH
 *
 */
public class MysqlDAO implements IDAO {
	private static MysqlDAO instance = new MysqlDAO();
	private static Connection conn;

	private MysqlDAO() {
		try {
			Properties pp = new Properties();
			//confPath:/D:/xampp/tomcat/wtpwebapps/TempMT_Index/WEB-INF/classes/Mysql.properties
			//root:D:\xampp\tomcat\wtpwebapps\TempMT_Index
			//confPath:/D:/xampp/tomcat/wtpwebapps/TempMT_Index/WEB-INF/classes/Mysql.properties
			//root:D:\xampp\tomcat\wtpwebapps\TempMT_Index
			//confPath:/D:/Users/CXH/Documents/java/workspaceWithJ2EE/TempMT_Index/build/classes/Mysql.properties
			//root:D:\Users\CXH\Documents\java\workspaceWithJ2EE\TempMT_Index
			String confPath = MysqlDAO.class.getClassLoader().getResource(DAOFactory.Mysql + ".properties").getFile();
			File f = new File(confPath);
			String root = f.getParentFile().getParentFile().getParentFile().getAbsolutePath();
			System.out.println("confPath:"+confPath);
			System.out.println("root:"+root);
			InputStream fis = MysqlDAO.class.getClassLoader().getResourceAsStream(DAOFactory.Mysql + ".properties");
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
			Logger.getLogger(MysqlDAO.class).error(e);
			e.printStackTrace();
		}
	}

	/**
	 * @param username
	 * @param password
	 */
	private MysqlDAO(String username, String password) {
		try {
			Properties pp = new Properties();
			InputStream fis = MysqlDAO.class.getClassLoader().getResourceAsStream(DAOFactory.Mysql + ".properties");
			pp.load(fis);
			String driver = pp.getProperty("driver");
			String url = pp.getProperty("url");
			String database = pp.getProperty("database");
			String parameter = pp.getProperty("parameter");
			Class.forName(driver);
			conn = DriverManager.getConnection(url + database + parameter, username, password);
		} catch (Exception e) {
			// 错误处理
			Logger.getLogger(MysqlDAO.class).error(e);
			e.printStackTrace();
		}
	}

	public static MysqlDAO getInstance() {
		return instance;
	}

	public static MysqlDAO getInstance(String username, String password) {
		instance = new MysqlDAO(username, password);
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
			// 错误处理
			Logger.getLogger(MysqlDAO.class).error(e);
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
			Logger.getLogger(MysqlDAO.class).error(e);
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
			Logger.getLogger(MysqlDAO.class).error(e);
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

}
