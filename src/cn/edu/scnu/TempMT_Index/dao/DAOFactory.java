/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年3月30日 上午10:05:16
 */
package cn.edu.scnu.TempMT_Index.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import cn.edu.scnu.TempMT_Index.service.CONTANT;

/**
 * @author CXH
 *
 */
public class DAOFactory {
	public static final String MySQL = "MySQL";
	public static final String Oracle = "Oracle";
	public static final String MSSQL = "MSSQL";

	/**
	 * 默认MySQL的数据库
	 * 
	 * @return
	 */
	public static IDAO getInstance() {
		return getInstance(MySQL);
	}

	public static IDAO getInstance(String DAOname) {
		if (!setDAOname(DAOname))
			return null;
		switch (DAOname) {
		case MySQL:
			return MySQLDAO.getInstance();
		case Oracle:
			// TODO
			return null;
		case MSSQL:
			return MSSQLDAO.getInstance();

		default:
			return null;
		}
	}

	public static IDAO getInstance(String DAOname, String username, String password) {
		if (!setProperties(DAOname, username, password))
			return null;
		return getInstance(DAOname);
	}

	public static boolean setProperties(String DAOname, String username, String password) {
		if (!setDAOname(DAOname))
			return false;
		if (!setUsername(username))
			return false;
		if (!setPassword(password))
			return false;
		return true;
	}

	/**
	 * @param password
	 * @return
	 */
	private static boolean setPassword(String password) {
		try {
			Properties pp = new Properties();
			InputStream fis = new FileInputStream(CONTANT.confPath + "\\" + "dbs.properties");
			pp.load(fis);
			String basetype = pp.getProperty("basetype");
			fis = new FileInputStream(CONTANT.confPath + "\\" + basetype + ".properties");
			pp.load(fis);
			pp.setProperty("password", password);
		} catch (IOException e) {
			Logger.getLogger(new Object() {
				// 静态方法中获取当前类名
				public Class<?> getClassForStatic() {
					return this.getClass();
				}
			}.getClassForStatic()).error(e);
			throw new RuntimeException();
		}
		return true;
	}

	/**
	 * @param username
	 * @return
	 */
	private static boolean setUsername(String username) {
		try {
			Properties pp = new Properties();
			InputStream fis = new FileInputStream(CONTANT.confPath + "\\" + "dbs.properties");
			pp.load(fis);
			String basetype = pp.getProperty("basetype");
			fis = new FileInputStream(CONTANT.confPath + "\\" + basetype + ".properties");
			pp.load(fis);
			pp.setProperty("username", username);
		} catch (IOException e) {
			Logger.getLogger(IDAO.class).error(e);
			throw new RuntimeException();
		}
		return true;
	}

	/**
	 * @param dAOname
	 * @return
	 */
	private static boolean setDAOname(String DAOname) {
		try {
			Properties pp = new Properties();
			InputStream fis = new FileInputStream(CONTANT.confPath + "\\" + "dbs.properties");
			pp.load(fis);
			pp.setProperty("basetype", DAOname);
		} catch (IOException e) {
			Logger.getLogger(DAOFactory.class).error(e);
//			throw new RuntimeException();
		}
		return true;
	}
}
