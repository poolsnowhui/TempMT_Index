/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年3月30日 上午10:05:16
 */
package cn.edu.scnu.TempMT_Index.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author CXH
 *
 */
public class DAOFactory {
	public static final String Mysql = "Mysql";
	public static final String Oracle = "Oracle";
	public static final String SQLserver = "SQLserver";

	public static IDAO getInstance() {
		return MysqlDAO.getInstance();
	}

	public static IDAO getInstance(String DAOname) {
		if (!setDAOname(DAOname))
			return null;
		switch (DAOname) {
		case Mysql:
			return MysqlDAO.getInstance();
		case Oracle:
			// TODO
			return null;
		case SQLserver:
			// TODO
			return null;

		default:
			return null;
		}
	}

	public static IDAO getInstance(String DAOname, String username, String password) {
		if (!setProperties(DAOname, username, password))
			return null;
		switch (DAOname) {
		case Mysql:
			return MysqlDAO.getInstance();
		case Oracle:
			// TODO
			return null;
		case SQLserver:
			// TODO
			return null;

		default:
			return null;
		}
	}

	private static boolean setProperties(String DAOname, String username, String password) {
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
			InputStream fis = DAOFactory.class.getClassLoader().getResourceAsStream("dbs.properties");
			pp.load(fis);
			String basetype = pp.getProperty("basetype");
			fis = IDAO.class.getClassLoader().getResourceAsStream(basetype + ".properties");
			pp.load(fis);
			pp.setProperty("password", password);
		} catch (IOException e) {
			Logger.getLogger(IDAO.class).error(e);
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
			InputStream fis = DAOFactory.class.getClassLoader().getResourceAsStream("dbs.properties");
			pp.load(fis);
			String basetype = pp.getProperty("basetype");
			fis = IDAO.class.getClassLoader().getResourceAsStream(basetype + ".properties");
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
			InputStream fis = DAOFactory.class.getClassLoader().getResourceAsStream("dbs.properties");
			pp.load(fis);
			pp.setProperty("basetype", DAOname);
		} catch (IOException e) {
			Logger.getLogger(IDAO.class).error(e);
			throw new RuntimeException();
		}
		return true;
	}
}
