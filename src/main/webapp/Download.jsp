<%@ page isELIgnored="false" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>  

<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>报告下载</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" />
</head>
<body>
<section class = "main">
<c:forEach var = "file" items = "${reports}">
	<c:url value = "download" var = "dc">
		<c:param name = "filename" value = "${file}"></c:param>
	</c:url>
	<a href = "${dc}" >${file}</a>
	<br>
</c:forEach>

<form action="upload" method = "post">
<input type = "submit" value = "返回">
</form>

</section>
</body>
</html>
