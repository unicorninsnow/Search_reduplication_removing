<%@page language="java" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<%@page import="cluster.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>



<%-- 
    Document   : results
    Created on : 2013-4-21, 15:12:21
    Author     : oubeichen
--%>
<%
	String content = request.getParameter("wd");
	if (content == null) {
		content = "";
	}
	content = URLDecoder.decode(content, "UTF-8");
	String pg = request.getParameter("page");//
	int nowpage = 1;
	if (pg == null) {
		nowpage = 1;
	}
	try {
		nowpage = Integer.parseInt(pg);
	} catch (Exception e) {
		nowpage = 1;
	}
	if (nowpage <= 0) {
		nowpage = 1;
	}
	/*TODO:数据库读取具体内容之前先读取行数，然后进行分页操作
	 */

	/*利用谷歌API来获取搜索结果用于测试*/
	//String gapiurl = "https://www.googleapis.com/customsearch/v1?key=AIzaSyC6H3srhmy5nYxDLh9hdSOoqoVfCoK7vKE&cx=016107310399893718915:kqe8vf9-tza&q="
	//+ URLEncoder.encode(content, "UTF-8")
	//+ "&alt=json"
	//+ "&start=" + ((nowpage - 1) * 10 + 1);
	Cluster fun = new Cluster();
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Result:<%=content %></title>
<script type="text/javascript" src="js/menu.js"></script>
<script type="text/javascript">
	//返回函数
	function onSearch() {
		var content = document.getElementById("content").value;
		content = encodeURIComponent(encodeURIComponent(content));
		//document.getElementById("content").value = encodeURI(content);
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
	//以下是异步刷新
	function makeajax(){
		url_quest = 'getrealurl.jsp?wd=' + encodeURI(encodeURI('<%=content %>')) + '&page=' + '<%=nowpage %>';
		makeRequest(url_quest);
	}
	function makeRequest(url) {
		http_request = false;
		if (window.XMLHttpRequest) {
			http_request = new XMLHttpRequest();
			if (http_request.overrideMimeType) {
				http_request.overrideMimeType('text/xml');
			}
		} else if (window.ActiveXObject) {
			try {
				http_request = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				try {
					http_request = new ActiveXObject("Microsoft.XMLHTTP");
				} catch (e) {
				}
			}
		}
		if (!http_request) {
			alert("您的浏览器不支持当前操作，请使用 IE 5.0 以上版本!");
			return false;
		}

		//定义页面调用的方法changeurl,没有();
		http_request.onreadystatechange = changeurl;
		http_request.open('GET', url, true);

		//禁止IE缓存
		http_request.setRequestHeader("If-Modified-Since", "0");

		//发送数据
		http_request.send(null);

	}

	function changeurl() {
		if (http_request.readyState == 4) {
			if (http_request.status == 0 || http_request.status == 200) {
				var result = http_request.responseText;
				var resultlist = result.split(',');
				var i = 0;
				for (i = 0; i < resultlist.length; i += 2) {
					var Id = 0;
					try {
						Id = parseInt(resultlist[i]);
						document.getElementById('url' + Id).innerHTML = resultlist[i + 1];
					} catch (e) {
						break;
					}
				}
			} else {//http_request.status != 200
			}
		}
	}
</script>
<link rel="stylesheet" type="text/css" media="all" href="css/style.css">
<link rel="stylesheet" type="text/css" href="css/buttons.css" />
</head>
<body onload="new Accordian('outerres',5,'header_highlight');makeajax();">
	<div id=head style="padding-top:20px;padding-bottom:15px;">
		<a href="index.jsp" class="s_logo"
			style="float:left;padding-right:20px"><img
			src="images/banner.png" width="100" height="38" border="0" alt="返回首页"
			title="返回首页"> </a>

		<div class="buttons">
			<input type="text" maxlength="2048" size="41" id="content" name="wd"
				value="<%=content%>" style="font-size:16px;width:300px; height:25px"
				onkeypress="if(event.keyCode==13||event.keyCode==108){onSearch();}" />
			<button type="submit" class="positive" onclick=onSearch();>
				<img src="images/apply2.png" alt="" /> Search
			</button>
		</div>
	</div>
	<div class="r" style="padding-top:20px">
		<%
			boolean flag = true,dontajax = true;
			if (content != null && content != "") {
				try {
					dontajax = fun.process_simple(content, nowpage);//判断是否需要进行后台处理
				} catch (Exception e) {
					flag = false;
					out.print("出错了！请稍后访问！");
					e.printStackTrace();
				}
		%>
		<ol id="outerres">
			<%
				int length = fun.getlist().size();
					if (flag) 
					{
						for (int i = 0; i < length; i++) {
							Clusteredresult_Node node = fun.getlist().get(i)
									.gethead(), nodep;
			%>
			<li style="list-style-type: none;">
				<div class="difres">
					<a href="<%=node.geturl()%>" onmousedown="" target="_blank"><%=node.gettitle()%></a>
					<div class="s">
						<div>
							<div class="url" id="url<%=node.getid()%>"
								style="white-space:nowrap;color: rgb(0, 153, 51);">
								<cite> <% //if(dontajax) {%> <%=node.geturl()%> <%//} %>
								</cite>
							</div>
							<div class="cont"></div>
							<span class="st" style="font-size: 13px;"><%=node.getabs()%></span>
						</div>
					</div>
					<!-- 
					<button type="submit" onclick="showSameres(this,'sres<%=i%>')">相同结果</button>
					<div id="sres<%=i%>" class="sres" style="display:none">
						<ul>
							<%nodep = node.getnext();
						while (nodep != null) {%>
							<li><a href="<%=nodep.geturl()%>"  target="_blank"><%=nodep.gettitle()%></a>
							</li>
							<%nodep = nodep.getnext();
						}%>
						</ul>
					</div>
					 -->
					<%
						nodep = node.getnext();
									if (nodep != null) {
					%>
					<div id="basic-accordian">
						<!--菜单开始-->
						<div id="test<%=i%>-header" class="accordion_headings">相似结果</div>
						<div id="test<%=i%>-content">
							<div class="accordion_child">
								<ul>
									<%
										while (nodep != null) {
									%>
									<li><a href="<%=nodep.geturl()%>" target="_blank"><%=nodep.gettitle()%></a>
										<div class="url" id="url<%=nodep.getid()%>"
											style="white-space:nowrap;color: rgb(0, 153, 51);">
											<%// if(dontajax) {%>
											<cite><%=nodep.geturl()%></cite>
											<%//} %>
										</div></li>
									<%
										nodep = nodep.getnext();
														}
									%>
								</ul>
							</div>
						</div>
					</div>
					<!--菜单结束-->
				</div> <%
 	}
 %>
			</li>
			<br />
			<%
				}
			%>
		</ol>
		<div class="bottom" style="color:blue; text-align:center">
			<%
				if (nowpage > 1) {
							out.print("<a href=results.jsp?wd="
									+ URLEncoder.encode(
											URLEncoder.encode(content, "UTF-8"),
											"UTF-8") + "&page=" + (nowpage - 1)
									+ ">上一页</a>&nbsp;&nbsp;&nbsp;" + "&nbsp;&nbsp;");
						}
						int pages = nowpage - 5;
						if(pages < 1)pages = 1;
						for(;pages < nowpage;pages++)
						{
									out.print("<a href=results.jsp?wd="
									+ URLEncoder.encode(
											URLEncoder.encode(content, "UTF-8"),
											"UTF-8") + "&page=" + (pages)
									+ ">"+ pages + "</a>&nbsp;");
						}
						out.print("第&nbsp;" + nowpage + "&nbsp;页&nbsp;");
						for(pages = nowpage + 1;pages <= nowpage + 5;pages++)
									out.print("<a href=results.jsp?wd="
									+ URLEncoder.encode(
											URLEncoder.encode(content, "UTF-8"),
											"UTF-8") + "&page=" + (pages)
									+ ">"+ pages + "</a>&nbsp;");
			%>
			<%
				out.print("<a href=results.jsp?wd="
								+ URLEncoder.encode(
										URLEncoder.encode(content, "UTF-8"),
										"UTF-8") + "&page=" + (nowpage + 1)
								+ ">下一页</a>");
			%>
			<%
				}
			}
			%>
		</div>
	</div>
</body>
</html>
