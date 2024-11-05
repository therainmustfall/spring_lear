<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
java.util.List<String> ids = java.nio.file.Files.readAllLines(java.nio.file.Path.of(request.getRealPath("/WEB-INF/class/stdid.txt")));
request.getContextPath();
request.setCharacterEncoding("UTF-8");
String sid = (String) request.getParameter("studentID");
String wid = (String) request.getParameter("myID");

if (wid != null && wid.trim().equals("fxc306")) 
{
	session.setAttribute("teaid", "Hello");
	response.sendRedirect("upload.jsp");
} else
{
	Boolean checked = false;
	for(String id : ids)
	{
		if (sid.trim().equals(id.trim()))
		{
			checked = true;
		}
	}

	if (checked)
	{
		session.setAttribute("stdid", sid);
		response.sendRedirect("download.jsp");
	} else
	{
		session.setAttribute("errMsg","输入正确的学号/工号");
		response.sendRedirect("login.jsp");
	}
}

%>


