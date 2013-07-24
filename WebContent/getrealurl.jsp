<%@ page language="java"  %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="cluster.*"%>
<%@ page import="java.net.URLDecoder"%>
<%
/*该页用于后台运行抓取操作，并返回真实url用于异步刷新*/
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
	Cluster fun = new Cluster();
	boolean flag = true,dontajax = true;
	if (content != null && content != "") {
		try {
			dontajax = fun.process(content, nowpage);//判断是否进行了后台处理
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		int length = fun.getlist().size();
		if (flag && !dontajax) 
		{
			for (int i = 0; i < length; i++) {
				Clusteredresult_Node node = fun.getlist().get(i).gethead(), nodep;		
				out.print(node.getid() + ",");	
				//out.print(node.gettitle() + "\n");	
				out.print(node.geturl() + ",");	
				nodep = node.getnext();
				while (nodep != null) {
					out.print(nodep.getid() + ",");
					//out.print(nodep.gettitle() + "\n");		
					out.print(nodep.geturl() + ",");	
					nodep = nodep.getnext();
				}
			}
		}
		else
		{
				out.print("出错了！请稍后访问！");
		}
	}
%>