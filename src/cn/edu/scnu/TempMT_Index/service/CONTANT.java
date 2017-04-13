/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年4月7日 下午10:44:37
 */
package cn.edu.scnu.TempMT_Index.service;

import java.io.File;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;

/**
 * @author CXH 公用常量
 */
public class CONTANT {
	public static final String rootPath = new File(
			CONTANT.class.getClassLoader().getResource(DAOFactory.Mysql + ".properties").getFile()).getParentFile()
					.getParentFile().getParentFile().getAbsolutePath();
	public static final String srcPath = System.getProperty("user.dir");
	public static final String labPath = rootPath + "\\lab";
	public static final String indexPath = rootPath + "\\tdindex";
	public static final long[][] testExample = { { 1, 10 }, { 1, 9 }, { 1, 8 }, { 1, 7 }, { 2, 7 }, { 2, 6 }, { 2, 5 },
			{ 2, 4 }, { 3, 4 }, { 2, 9 }, { 3, 9 }, { 5, 10 }, { 6, 10 }, { 2, 8 }, { 3, 8 }, { 4, 9 }, { 1, 9 },
			{ 5, 9 }, { 7, 9 }, { 5, 8 }, { 7, 8 }, { 4, 7 }, { 5, 7 }, { 6, 7 }, { 5, 6 }, { 3, 5 }, { 4, 5 } };
}
