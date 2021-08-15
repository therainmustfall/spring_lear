<%@ page isELIgnored="false" %>
<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>  

<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>确认和数据处理方式选择</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" />


</head>
<body>
<section class = "main">
<h1>${upload_message}</h1>
<form action = "upload" method = "post">
 <h3> 自引、他引划分依据</h3>
委托人(";"分号分隔）: <input type = "text" name = "apply_author" />  <br/>
课题组(";"分号分隔）: <input type = "text" name = "resear_group" />  <br/>
<br/>

<input type = "checkbox" name = "first_author" /> 按第一作者  <br/>
<!--
<input type = "checkbox" name = "corre_author" /> 按通讯作者  <br/>
-->

<h3>报告额外添加项</h3>
<input type = "checkbox" name = "jif" />  包含影响因子 <br/>
<input type = "checkbox" name = "detail_list" />  包含他引文献详表 <br/>
<br/>
<input type = "submit" name = "upload" value = "取消">
<input type = "submit" name = "process" value = "处理">
</form>
</section>
</body>
</html>
