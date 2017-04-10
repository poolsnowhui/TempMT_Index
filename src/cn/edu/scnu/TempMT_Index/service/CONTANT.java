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
 * @author CXH
 *
 */
public class CONTANT {
	public static final String rootPath = new File(
			CONTANT.class.getClassLoader().getResource(DAOFactory.Mysql + ".properties").getFile()).getParentFile()
					.getParentFile().getParentFile().getAbsolutePath();
	public static final String srcPath = System.getProperty("user.dir");
}
