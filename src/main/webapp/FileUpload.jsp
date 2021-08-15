<%@ page isELIgnored="false" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>  
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" />
<title>文献上传</title>
</head>
<body>
<section class = "main">

<b>生成的报告相关文件</b>:<br/>
<c:forEach var = "file" items = "${reports}">
	<c:url value = "download" var = "dc">
		<c:param name = "filename" value = "${file}"></c:param>
	</c:url>
	<a href = "${dc}" >${file}</a>
	<br>
</c:forEach>
<br>


<form action = "upload" method = "post">
	已上传了<strong>${empty citing_files ? 0:citing_files}</strong>个引用文件,<strong>${empty paperlist ? 0:paperlist}</strong>个列表文献 ==> <input type = "submit" name = "clear_reports" value = "清空"><br>
</form>

<br>
<form action="upload" method = "post" enctype = "multipart/form-data">


作者文献列表(savedrecs.txt) ===> <input type = "file" name = "paper_list" value = "文献列表">
<br>
<br>
以入藏号命名的引用文件  ========> <input type = "file" name = "files" value = "引用数据" multiple = "multiple">
<br>
<br>
<br>
<input type = "submit" name = "upload_records" value = "上传">
<input type = "submit" name = "process" value = "直接进入处理">
</form>
</section>
</body>
</html>
