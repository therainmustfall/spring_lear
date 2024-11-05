<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% 
	if (session.getAttribute("stdid") == null) response.sendRedirect("login.jsp");
%> 
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta charset="utf-8">
<title>作业项目文件下载</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" />
</head>
<body>
<%
java.io.File   uploadFolder = new java.io.File(request.getRealPath("/WEB-INF/uploads"));
List<String> list = new ArrayList<>();
if (uploadFolder.list().length > 0) {
	for(java.io.File f: uploadFolder.listFiles()){
	list.add(f.getName());
}
}
request.getSession().setAttribute("files", list);
%>

<section class = "main">
<c:forEach var = "file" items = "${files}">
	<c:url value = "download" var = "dc">
		<c:param name = "filename" value = "${file}"></c:param>
	</c:url>
	<a href = "${dc}" >${file}</a>
	<br>
</c:forEach>


<a href="logout.jsp">退出</a>


</section>
</body>
</html>
