/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年3月29日 下午4:18:25
 */
package cn.edu.scnu.TempMT_Index.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * @author CXH
 *
 */
public interface IDAO {
	
	public List<Object[]> executeQuery(String sql) throws SQLException;
	public ResultSet executeQuery(String []sql) throws SQLException;
	public int execute(String... sql) throws SQLException;
	public boolean callPro(String sql) throws SQLException;
	public Connection getConnection();
	public void closeIDAO() throws SQLException;
}
