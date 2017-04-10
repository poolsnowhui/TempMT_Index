/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年3月30日 上午9:53:45
 */
package demo;

import java.sql.SQLException;
import java.util.List;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;
import cn.edu.scnu.TempMT_Index.dao.IDAO;

/**
 * @author CXH
 *
 */
public class DAODemo {
	public static void main(String[] args) {
		/*
		 */
		IDAO mysql = DAOFactory.getInstance(DAOFactory.Mysql);
		try {
			List<Object[]> executeQuery = mysql.executeQuery("select *c from student20k;");
			for (Object[] objects : executeQuery) {
				for (int i = 0; i < objects.length; i++) {
					System.out.print(objects[i] + " ");
				}
				System.out.println();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
