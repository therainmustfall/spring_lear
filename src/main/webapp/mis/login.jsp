<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>  
<%
Object errMsg = session.getAttribute("errMsg");
if (errMsg != null) {%>
<span><%=errMsg%></span>
<%session.removeAttribute("errMsg");} %>


<form method="post" action="check.jsp">
工号 <input type="password" name="myID" placeholder="教师填写工号">
学号 <input type="password" name="studentID" placeholder="学生填写学号">
<br>
<input type="submit" name="submit" value="登录">
</form>