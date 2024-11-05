<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% 
	if (session.getAttribute("stdid") == null) response.sendRedirect("login.jsp");
%> 
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" />
<title>文件上传</title>
</head>
<body>
<section class = "main">

<%
java.io.File   uploadFolder = new java.io.File(request.getRealPath("/WEB-INF/uploads"));
if (!uploadFolder.exists()) uploadFolder.mkdir();

List<String> list = new ArrayList<>();
if (uploadFolder.list().length > 0) {
	for(java.io.File f: uploadFolder.listFiles()){
	list.add(f.getName());
}
}
request.getSession().setAttribute("files", list);
request.getSession().setAttribute("filesnumber", list.size());
%>
<form action="upload" method = "post">
	<h2>导出文档<strong>${empty filesnumber ? 0:filesnumber}</strong>个</h2>
	<input type = "submit" name = "clearReports" value = "清空文件夹"><br>
	<br/>
	<b>列表</b>:<br/>
	<c:forEach var = "file" items = "${files}">
		<c:url value = "download" var = "dc">
			<c:param name = "filename" value = "${file}"></c:param>
		</c:url>
		<a href = "${dc}" >${file}</a>
		<br>
	</c:forEach>
	<br />
	<br/>
</form>
<form action = "upload" method = "post" enctype = "multipart/form-data">
	<br>
	<hr>
	<h2>查询</h2>
	<span style="width:150px;display:inline-block;">起始年</span>        <input type = "text" name = "year_range" placeholder = "如2021-2022" /> 
	<br />
	<br />
	<span style="width:150px;display:inline-block;">收录号（文件）</span>    <input type = "file" name = "uts_file" /> 
	<br/>
	<br/>   
	<span style="width:150px;display:inline-block;">人员信息（文件）</span>   <input type = "file" name = "resear_group_file" />  <br/>
<br/>

<input type = "submit" name = "upload" value = "取消">
<input type = "submit" name = "process" value = "查询">

</form>


<br>
<br>
<a href="logout.jsp">退出</a>
</section>
</body>
</html>
