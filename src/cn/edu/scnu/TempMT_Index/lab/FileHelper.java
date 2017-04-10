package cn.edu.scnu.TempMT_Index.lab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import cn.edu.scnu.TempMT_Index.service.CONTANT;

/**
 * 文件单例模式
 * 
 * @author CXH
 *
 */
public class FileHelper {
	private static FileHelper instance = null;

	private FileHelper() {
	}

	static {
		instance = new FileHelper();
	}

	public static FileHelper getInstance() {
		return instance;
	}

	private static FileWriter fw;
	private static FileReader fr;
	private static BufferedReader br;
	private static String dirName;

	static {
		dirName = CONTANT.rootPath + "\\lab";
		fw = null;
		fr = null;
		br = null;
	}

	public void write(String fileName, String data) {
		// 写文件
		File file = new File(dirName, fileName);
		file.getParentFile().mkdirs();// 建目录
		try {
			fw = new FileWriter(file, true);// 设置追加写
			// System.out.println(fileName);
			fw.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	public  String read(String fileName) {
		// 读文件
		File file = new File(dirName, fileName);
		String data = "";
		if (file.exists()) {
			try {
				fr = new FileReader(file);
				// System.out.println(fileName);
				br = new BufferedReader(fr);
				String line = "";
				while ((line = br.readLine()) != null) {
					data += line;
					data += "\n";
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close();
			}
		} else {
			throw new RuntimeException("目录" + dirName + "中的" + fileName + "文件不存在");
		}
		return data;
	}

	private void close() {
		if (fr != null) {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fr = null;
		}
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fw = null;
		}
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			br = null;
		}
	}

	public static void main(String[] args) {
		FileHelper fh = FileHelper.getInstance();
		fh.write("test", "abcderf什么鬼" + "\n" + "dsfaasdf");
		System.out.println(fh.read("test"));
	}
}
