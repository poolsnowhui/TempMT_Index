/**
 * Copyright 2017 TempMT_Index
 * All right reserved.
 *
 * author:CXH
 * create date: 2017年4月10日 下午8:39:30
 */
package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import cn.edu.scnu.TempMT_Index.service.CONTANT;

/**
 * @author CXH
 *
 */
public class FileScanner {
	public static void scan(String path) {
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {
		    inputStream = new FileInputStream(path);
		    sc = new Scanner(inputStream, "UTF-8");
		    while (sc.hasNextLine()) {
		        String line = sc.nextLine();
		         System.out.println(line);
		    }
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		}catch(IOException e){
			Logger.getLogger(FileScanner.class.getName()).error(e);
			e.printStackTrace();
		}
		finally {
		    if (inputStream != null) {
		        try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    if (sc != null) {
		        sc.close();
		    }
		}
	}
	public static void main(String[] args) {
		FileScanner.scan(CONTANT.rootPath+"\\1.dat");
	}
}
