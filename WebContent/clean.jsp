<%@ page language="java" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<%@page import="java.sql.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Cleaning...</title>
</head>
<body>
正在清理数据库，2秒后返回..
 <% 
 		Class.forName("com.mysql.jdbc.Driver");	
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/searchdb", "search", "search");
		Statement stmt = conn.createStatement();
		String sql = "DELETE FROM RESULTTABLE WHERE ID > 5";
		stmt.executeUpdate(sql);
		sql = "DELETE FROM KEYWORDTABLE WHERE ID > 5";
		stmt.executeUpdate(sql);
		out.println("<meta http-equiv='refresh' content='2;url=index.jsp'>");
 %>
</body>
</html>