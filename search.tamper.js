// ==UserScript==
// @name       Search script
// @namespace  http://oubeichen.com/
// @version    0.1
// @description  创新项目用脚本，用于在百度搜索时，自动显示链接到相同关键词和相同页码的创新项目搜索结果
// @include    http://www.baidu.com/s*
// @include    http://baidu.com/s*
// @copyright  2014, oubeichen
// ==/UserScript==

//定义一下本机创新项目的域名
var host = "http://localhost:8899/Search_reduplication_removing/";

function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]); return null;
}
var page = getQueryString("pn");
if(page === null)
    page = 1;
else
    page = page / 10 + 1;
//由于我们一页就有2page，所以要除以2.
page = parseInt((page + 1) / 2);
var keyword = getQueryString("wd");

//以下是异步刷新
function makeajax(){
	var url_quest = host + 'getrealurl.jsp?wd=' + encodeURIComponent(encodeURIComponent(keyword)) + '&page=' + page;
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
		} catch (excep) {
			try {
				http_request = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (except) {
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
    var inserthtml = "<a href='" + host + "results.jsp?wd="
									+ encodeURIComponent(encodeURIComponent(keyword)) +
								    "&page=" + page
									+ "' target='_blank'>  查看合并后的结果  </a>";
    var d=document.getElementById("s_tab");
	if (http_request.readyState == 4) {
		if (http_request.status == 200) {
            d.innerHTML = d.innerHTML + "&nbsp;&nbsp;" +inserthtml;
            
		} else {//http_request.status != 200
            //服务器有问题，do nothing
		}
	}
}

makeajax();