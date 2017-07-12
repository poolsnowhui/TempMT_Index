/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年4月7日 下午10:44:37
 */
package cn.edu.scnu.TempMT_Index.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author CXH 公用常量
 */
public class CONTANT {
	public static String rootPath = new File(CONTANT.class.getClassLoader().getResource("/").getFile()).getParentFile().getParentFile().getAbsolutePath();
//	public static final String rootPath = new File(
//			CONTANT.class.getClassLoader().getResource(DAOFactory.MySQL + ".properties").getFile()).getParentFile()
//					.getParentFile().getParentFile().getAbsolutePath();
	public static String confPath = new File(CONTANT.class.getClassLoader().getResource("/").getFile()).getAbsolutePath();
	public static String srcPath = System.getProperty("user.dir");
	public static final String labPath = rootPath + "\\lab";
	public static final String indexPath = rootPath + "\\tdindex";
	public static final String logPath =rootPath+"\\logs";
	public static final long[][] testExample = { { 1, 10 }, { 1, 9 }, { 1, 8 }, { 1, 7 }, { 2, 7 }, { 2, 6 }, { 2, 5 },
			{ 2, 4 }, { 3, 4 }, { 2, 9 }, { 3, 9 }, { 5, 10 }, { 6, 10 }, { 2, 8 }, { 3, 8 }, { 4, 9 }, { 1, 9 },
			{ 5, 9 }, { 7, 9 }, { 5, 8 }, { 7, 8 }, { 4, 7 }, { 5, 7 }, { 6, 7 }, { 5, 6 }, { 3, 5 }, { 4, 5 } };
	static{
		try {
			rootPath = URLDecoder.decode(rootPath, "UTF-8");
			confPath = URLDecoder.decode(confPath, "UTF-8");
			srcPath = URLDecoder.decode(srcPath, "UTF-8");
			System.out.println("rootPath:"+rootPath);
			System.out.println("confPath:"+confPath);
			System.out.println("srcPath:"+srcPath);
			System.out.println("logPath:"+logPath);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
