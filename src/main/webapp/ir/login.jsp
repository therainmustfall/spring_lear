<%@ page language="java" import="java.util.*" pageEncoding="utf8"%> 
 
<%
Object errMsg = session.getAttribute("errMsg");
if (errMsg != null) {
%>
<span><%=errMsg%></span>

<% 
session.removeAttribute("errMsg");
} %>

<html>
<head> 
<title>登录</title>
<meta name="viewport" content="width=device-width,initial-scale=1"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" />
</head>
<body>
<section class="main">
	<form method="post" action="check.jsp">
		工号 <input type="password" name="myID" placeholder="工号">
		<br>
		<br>
		<input type="submit" name="submit" value="登录">
	</form>
</section>
</body>
</html>