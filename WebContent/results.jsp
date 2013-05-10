<%@ page language="java" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<%@ page import="java.net.URLDecoder"%>
<%-- 
    Document   : results
    Created on : 2013-4-21, 15:12:21
    Author     : oubeichen
--%>
<%
	String content = URLDecoder.decode(request.getParameter("wd"),"UTF-8");
	if (content == null) {
		content = "";
	}
	/*TODO:数据库读取具体内容之前先读取行数，然后进行分页操作
	 */
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Result</title>
<script type="text/javascript" src="js/menu.js"></script>
<script type="text/javascript">
	//返回函数
	function onBackHome() {
		window.location.href = "index.jsp";
	}
	function onSearch() {
		var content = document.getElementById("content").value;
		content = encodeURI(encodeURI(content));
		window.location.href = "results.jsp?wd=" + content;
	}
	function show(obj) {
		frameAll();
		document.getElementById('s' + obj.id).style.display = 'block';
	}
	function frameAll(obj) {
		var all = document.getElementById('all').childNodes;
		for ( var i = 0; i < all.length; i++) {
			all[i].style.display = 'none';
		}
	}
	function showSameres(e, id) {
		var tooltip = document.getElementById(id);
		if (tooltip.style.display != 'none') {
			tooltip.style.display = 'none';
			return;
		}
		tooltip.style.postion = 'absolute';
		tooltip.style.top = e.offsetTop + 20;
		tooltip.style.left = e.offsetLeft + 20;
		tooltip.style.display = 'block';
	}
</script>
<link rel="stylesheet" type="text/css" media="all" href="css/style.css">
<link rel="stylesheet" type="text/css" href="css/buttons.css"/>
</head>
<body onload="new Accordian('outerres',5,'header_highlight');">
	<div id=head>
		<a href="/" class="s_logo" onmousedown="onBackHome()"><img
			src="images/banner.png" width="100" height="38" border="0" alt="返回首页"
			title="返回首页"> </a>
	</div>
	<div class="buttons">
		<input type="text" maxlength="2048" size="41" id="content"
			name="txtContent" value="<%=content%>"
			style="font-size:16px;width:300px; height:25px;" />
		<button type="submit" class="positive" onclick="onSearch()">
			<img src="images/apply2.png" alt="" /> Search
		</button>
	</div>
<div class="r">
	<ol id="outerres">
		<li style="list-style-type: none;">
			<div class="difres">
				<a href="http://www.ymzj.cn/thread-499-1-1.html" onmousedown=""
					target="_blank">HTML5实例教程</a>
				<div class="s">
					<div>
						<div class="url"
							style="white-space:nowrap;color: rgb(0, 153, 51);">
							<cite>www.ymzj.cn/thread-499-1-1.html</cite>
						</div>
						<div class="cont"></div>
						<span class="st" style="font-size: 13px;">HTML5实例教程:<em>OL标签</em>的start属性和reversed属性,源码之家.
							<b>...</b> start属性用来定义列表<em>编号</em>的起始位置，比如下面的代码，列表将从50开始51...55以此类推
							&lt;ol <b>...</b> </span>
					</div>
				</div>
				<button type="submit"
					onclick="showSameres(this,'sres1')">相同结果1</button>
				<div id="sres1" class="sres" style="display:none">
					<ul>
						<li>a</li>
						<li>b</li>
						<li>c</li>
					</ul>
				</div>
				<div id="basic-accordian">
					<!--菜单开始-->
					<div id="test-header" class="accordion_headings">相同结果1</div>
					<div id="test-content">
						<div class="accordion_child">
							<ul>
								<li><a href="">网页A</a></li>
								<li><a href="">网页B</a></li>
							</ul>
						</div>
					</div>
				</div>
				<!--菜单结束-->

			</div></li>
					<li style="list-style-type: none;">
			<div class="difres">
				<a href="http://www.ymzj.cn/thread-499-1-1.html" onmousedown=""
					target="_blank">HTML5实例教程</a>
				<div class="s">
					<div>
						<div class="url"
							style="white-space:nowrap;color: rgb(0, 153, 51);">
							<cite>www.ymzj.cn/thread-499-1-1.html</cite>
						</div>
						<div class="cont"></div>
						<span class="st" style="font-size: 13px;">HTML5实例教程:<em>OL标签</em>的start属性和reversed属性,源码之家.
							<b>...</b> start属性用来定义列表<em>编号</em>的起始位置，比如下面的代码，列表将从50开始51...55以此类推
							&lt;ol <b>...</b> </span>
					</div>
				</div>
				<button type="submit"
					onclick="showSameres(this,'sres2')">相同结果2</button>
				<div id="sres2" class="sres" style="display:none">
					<ul>
						<li>a</li>
						<li>b</li>
						<li>c</li>
					</ul>
				</div>
				<div id="basic-accordian">
					<!--菜单开始-->
					<div id="test1-header" class="accordion_headings">相同结果2</div>
					<div id="test1-content">
						<div class="accordion_child">
							<ul>
								<li><a href="">网页A</a></li>
								<li><a href="">网页B</a></li>
							</ul>
						</div>
					</div>
				</div>
				<!--菜单结束-->

			</div></li>
	</ol>
</div>
</body>
</html>
