<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
session.removeAttribute("stdid");
response.sendRedirect("login.jsp");
%>