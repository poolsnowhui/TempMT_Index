<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE HTML>
<html>
<head>
<base href="<%=basePath%>" >
<title>TempMT_Index</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="时态数据库,多线程,时态索引">
<meta http-equiv="description" content="基于多线程的时态数据查询研究与实现">
<link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>
	<jsp:forward page="/WEB-INF/MainFrame.html"></jsp:forward>
</body>
</html>
