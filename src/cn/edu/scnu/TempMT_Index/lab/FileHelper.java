package cn.edu.scnu.TempMT_Index.lab;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import cn.edu.scnu.TempMT_Index.service.CONTANT;
import demo.FileScanner;

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

	public void writeBinary(String fileName, String data) {
		// 写文件
		DataOutputStream dos = null;
		try {
			File file = new File(dirName, fileName);
			file.getParentFile().mkdirs();// 建目录
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			dos.writeBytes(data);
		} catch (IOException e) {
			Logger.getLogger(FileHelper.class).error(e);
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String read(String fileName) {
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
		} catch (IOException e) {
			Logger.getLogger(FileScanner.class.getName()).error(e);
			e.printStackTrace();
		} finally {
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

	/**
	 * @param dirName
	 *            the dirName to set
	 */
	public void setDirName(String dirName) {
		FileHelper.dirName = dirName;
	}

	public static void main(String[] args) {
		FileHelper fh = FileHelper.getInstance();
		fh.setDirName(CONTANT.rootPath);
		String s = "";
		for (int i = 0; i < Math.pow(2, 15); i++) {
			s += "test123" + i + "\n";
		}
		fh.writeBinary("1.dat", s);
		fh.setDirName(CONTANT.rootPath + "\\lab");
		System.out.println("finished");
	}
}
