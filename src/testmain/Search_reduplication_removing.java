package testmain;
import java.io.*;
/*
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
*/
import java.util.*;
/*
import java.util.Set;
import java.util.TreeSet;
*/

import org.apache.commons.httpclient.*;
/*
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
*/
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/*
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
*/
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.util.*;
import org.htmlparser.tags.LinkTag;

import crawl.Search_engine_process;
import crawl.Search_word_process;
import datapackage.Link_queue;

public class Search_reduplication_removing {

	
	public class DownLoadFile{
		/**
		* 根据URL 和网页类型生成需要保存的网页的文件名，去除URL 中的非文件名字符
		*/
		
		byte[] responseBody;
		public String getFileNameByUrl(String url,String contentType)
		{
			//移除http://
			url = url.substring(7);
			//text/html类型
			if(contentType.indexOf("html") != -1)
			{
				url = url.replace("[\\?/:*|<>\"]", "_") + ".html";
				return url;
			}
			else//如application/pdf类型
			{
				return url.replace("[\\?/:*|<>\"]", "_") + "." +
					contentType.substring(contentType.lastIndexOf("/") + 1);
			}
		}
		
		/**
		* 保存网页字节数组到本地文件，filePath 为要保存的文件的相对地址
		*/
		private void saveToLocal(byte[] data,String filePath){
			try{
				DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(filePath)));
				for (int i = 0; i < data.length; ++i)
						out.write(data[i]);
				out.flush();
				out.close();
			}	catch	(IOException e){
				e.printStackTrace();
			}
		}
		//下载URL指向的网页
		public String downloadFile(String url/*,String filePath*/){
			String filePath = null;
			HttpClient httpClient = new HttpClient();
			httpClient.getHostConfiguration().setProxy("127.0.0.1", 8118);
			//设置HTTP连接超时5s
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			GetMethod getMethod = new GetMethod(url);
			//设置get请求超时5s
			getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
			//设置请求重试处理
			getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			
			//执行HTTP GET请求
			try {
				int statusCode = httpClient.executeMethod(getMethod);
				// 判断访问的状态码
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + getMethod.getStatusLine());
					filePath = null;
				}

				// 处理HTTP响应内容
				responseBody = getMethod.getResponseBody();// 读取为字节数组
				// 根据网页url 生成保存时的文件名
				/*filePath = "temp\\"
						+ getFileNameByUrl(url,
								getMethod.getResponseHeader("Content-Type")
										.getValue());*/
				//filePath = "test_117.htm";
//				saveToLocal(responseBody, filePath);
			} catch (HttpException e) {
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				System.out.println("Please check your provided http address!");
				e.printStackTrace();
			} catch (IOException e) {
				// 发生网络异常
				e.printStackTrace();
			} finally {
				// 释放连接
				getMethod.releaseConnection();
			}
			return filePath;
		}
	}

	public interface LinkFilter {
		public boolean accept(String url);
	}


	public static void main(String[] args) throws HttpException, IOException, ParserException {
		Search_reduplication_removing testtest = new Search_reduplication_removing();
		DownLoadFile downLoader=testtest.new DownLoadFile(); 
		//HtmlParserTool htmlparsertool = testtest.new HtmlParserTool();
		
		Search_word_process searchword = new Search_word_process();
		searchword.putin();
		searchword.choose_engine_search_word();
		searchword.access_to_appointed_page();
		
		Search_engine_process search_engine_process = new Search_engine_process();
		search_engine_process.extractLinks(searchword.getsearch_url(), searchword.getsearch_mode(), searchword.getNoofpagetoaccess());
		
		
		Link_queue result_links = search_engine_process.getresult_links();
		result_links.output_all_links();
		
		/*Set<String> search_result_links = htmlparsertool.choose_engine_search_word();
		
		System.out.println("468416358413641364163413");
		System.out.println(search_result_links);
		System.out.println("468416358413641364163413");
		
		
		Pages_analysis page_analysis = testtest.new Pages_analysis();
		page_analysis.analyze_pages(search_result_links);
		*/
		//page_analysis.get_page_mainbody("http://blog.sina.com.cn/s/blog_4caedc7a0102drdo.html");
		//page_analysis.get_page_title("http://news.cnblogs.com/n/110434/");
		
		
		
		
		/*
		HttpClient httpclient = new HttpClient();
		//URLEncoder.encode(src);
		GetMethod getMethod = new GetMethod("http://www.baidu.com/s?wd=C99%D0%C2%CC%D8%D0%D4&rsv_bp=0&rsv_spt=3&rsv_sug3=4&rsv_sug=0&rsv_sug1=4&rsv_sug4=222&inputT=10056");
		//
		//("http://www.baidu.com");
		//getMethod.addRequestHeader("Content-type" , "text/html; charset=utf-8");
		//httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		int statusCode = httpclient.executeMethod(getMethod);
		OutputStream output = new FileOutputStream("test_baidu_115.htm");
		//PrintStream   old   =   System.out;
		PrintStream myOut = new PrintStream(output);
		System.out.println(statusCode);
		System.setOut(myOut);
		//String response = getMethod.getResponseBodyAsString();
		
		InputStream resStream = getMethod.getResponseBodyAsStream();  
        BufferedReader br = new BufferedReader(new InputStreamReader(resStream));  //,"utf-8"
        StringBuffer resBuffer = new StringBuffer();  
        */
        /*
        String resTemp = "";
        while((resTemp =  br.readLine()) != null){  
            resBuffer.append(resTemp);
            resBuffer.append("\n");
        	System.out.println(resTemp);
        }  
        */
		/*
        int resTemp = -1;
        while((resTemp =  br.read()) != -1){  
            resBuffer.append((char)resTemp);
        }
        String response = resBuffer.toString();  
        System.out.println(response);
		
		getMethod.releaseConnection();
		*/
		return ;
	}
	
}