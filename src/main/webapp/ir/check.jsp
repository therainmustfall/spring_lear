<%@page import="java.io.BufferedReader"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.File"%>
<%@page import="java.nio.file.Path"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
File file = new File(request.getRealPath("/WEB-INF/class/stdid.txt"));
String line="";
FileReader fr = new FileReader(file);
BufferedReader br = new BufferedReader(fr);
List<String> ids = new ArrayList<>();
while((line = br.readLine()) != null)
{
	ids.add(line.trim());
}
br.close();

request.setCharacterEncoding("UTF-8");
String wid = (String) request.getParameter("myID").trim();

Boolean checked = false;
for(String id : ids)
{
	if (wid.equals(id.trim()))
	{
		checked = true;
	}
}

if (checked)
{
	session.setAttribute("stdid", wid);
	response.sendRedirect("download.jsp");
} else
{
	session.setAttribute("errMsg","输入正确的工号");
	response.sendRedirect("login.jsp");
}


%>


