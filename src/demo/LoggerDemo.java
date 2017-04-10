package demo;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerDemo {
	public static void main(String[] args) {
		Logger log1 = Logger.getLogger("LoggerDemo");
		log1.setLevel(Level.INFO);
		log1.info("fine");
		Logger log2 = Logger.getLogger("LoggerDemo2");
		log2.setLevel(Level.WARNING);
		log2.fine("fine");
		System.out.println(System.getProperty("java.class.path"));//user.dir指定了当前的路径 
		String s = LoggerDemo.class.getClassLoader().getResource("Mysql.properties").getFile();
		System.out.println(s);
		File f = new File(s);
		String ab = f.getParentFile().getParentFile().getParentFile().getAbsolutePath();
		System.out.println(ab);
		//		三月 30, 2017 1:47:39 下午 demo.LoggerDemo main
//		信息: fine
//		D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\build\classes;D:\xampp\tomcat\lib\annotations-api.jar;D:\xampp\tomcat\lib\catalina-ant.jar;D:\xampp\tomcat\lib\catalina-ha.jar;D:\xampp\tomcat\lib\catalina-tribes.jar;D:\xampp\tomcat\lib\catalina.jar;D:\xampp\tomcat\lib\commons-logging-1.2.jar;D:\xampp\tomcat\lib\ecj-4.4.2.jar;D:\xampp\tomcat\lib\ecj-4.4.jar;D:\xampp\tomcat\lib\el-api.jar;D:\xampp\tomcat\lib\jasper-el.jar;D:\xampp\tomcat\lib\jasper.jar;D:\xampp\tomcat\lib\jsp-api.jar;D:\xampp\tomcat\lib\servlet-api.jar;D:\xampp\tomcat\lib\tomcat-api.jar;D:\xampp\tomcat\lib\tomcat-coyote.jar;D:\xampp\tomcat\lib\tomcat-dbcp.jar;D:\xampp\tomcat\lib\tomcat-i18n-es.jar;D:\xampp\tomcat\lib\tomcat-i18n-fr.jar;D:\xampp\tomcat\lib\tomcat-i18n-ja.jar;D:\xampp\tomcat\lib\tomcat-jdbc.jar;D:\xampp\tomcat\lib\tomcat-util.jar;D:\xampp\tomcat\lib\tomcat7-websocket.jar;D:\xampp\tomcat\lib\websocket-api.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\commons-beanutils-1.8.0.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\commons-chain-1.2.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\commons-digester-2.0.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\commons-logging-1.1.3.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\json.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\log4j-1.2.13.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\mysql-connector-java-5.1.6-bin.jar;D:\Users\CXH\Documents\java\workspaceWithJ2EE\tempdb\WebRoot\WEB-INF\lib\struts-core-1.3.10.jar
//		/D:/Users/CXH/Documents/java/workspaceWithJ2EE/tempdb/build/classes/cn

	}
}
