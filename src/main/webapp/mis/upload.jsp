<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% 
	if (session.getAttribute("teaid") == null) response.sendRedirect("login.jsp");
request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");
%> 
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" />
<title>文献上传</title>
</head>
<body>
<section class = "main">

<b>已上传文件</b>:<br/>
<c:forEach var = "file" items = "${files}">
	<c:url value = "download" var = "dc">
		<c:param name = "filename" value = "${file}"></c:param>
	</c:url>
	<a href = "${dc}" >${file}</a>
	<br>
</c:forEach>
<br>

<form action = "upload" method = "post">
	已上传<strong>${empty filesnumber ? 0:filesnumber}</strong>个文件 ==> <input type = "submit" name = "clearReports" value = "清空文件夹"><br>
</form>

<br>
<form action="upload" method = "post" enctype = "multipart/form-data">

<br>
<br>
<input type = "file" name = "files" value = "课程文档上传" multiple = "multiple">
<br>
<br>
<br>
<input type = "submit" name = "uploadfiles" value = "上传">
</form>

<br>
<br>
<a href="logout.jsp">退出</a>
</section>
</body>
</html>
