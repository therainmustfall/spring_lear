<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>机构库数据导出</title>
</head>
<body>
<section class = "main">
<h1>${upload_message}</h1>
<form action = "upload" method = "post">
	起始年份：     <input type = "text" name = "year_range" placeholder = "开始年-结束年，如2021-2022" /> <br />
	收录号(粘贴):       <input type = "text" name = "uts" />          <br/>
	<hr>
	收录号（文件）： <input type = "file" name = "uts_file" />           <br/>
	人员信息（粘贴）:     <input type = "text" name = "resear_group" />  <br/>
	<hr>
	人员信息（文件）:     <input type = "text" name = "resear_group_file" />  <br/>
<br/>

<input type = "submit" name = "upload" value = "取消">
<input type = "submit" name = "process" value = "查询">
</form>
</section>
</body>
</html>