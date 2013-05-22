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

public class Search_reduplication_removing {

	public class Result_Link_Struct{
		String link_title;		//链接标题 (String)
		String link_url;		//链接URL (String)
		String link_abstract;	//链接摘要 (String)
		String link_text;		//链接正文 (String)
		int link_page_from;		//链接来自原第几个搜索结果页面 (int)
		int cluster_id;			//聚合后所在的组
		
		public void output(){
			System.out.println("链接标题： " + link_title);
			System.out.println("链接url： " + link_url);
			System.out.println("链接摘要： " + link_abstract);
			//System.out.println("链接正文： " + link_text);
			//System.out.println("链接所在页码： " + link_page_from);
			System.out.println("***************************************");
		}
	}
	
	public class Link_queue{
		
		private LinkedList<Result_Link_Struct> link_queue_list = new LinkedList<Result_Link_Struct>();
		public void add_link(Result_Link_Struct link){
			link_queue_list.addLast(link);
		}
		public void create_new_link(){
			Result_Link_Struct new_link = new Result_Link_Struct();
			link_queue_list.addLast(new_link);
			return ;
		}
		public void remove_link(){
			link_queue_list.removeFirst();
		}
		public Result_Link_Struct get_last_link(){
			return link_queue_list.getLast();
		}
		public void output_all_links(){
			for(int i = 0;i < link_queue_list.size();++i){
				link_queue_list.get(i).output();
			}
		}
	}

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

	public class HtmlParserTool {
		private char search_mode = 'B';//默认以百度作为搜索引擎
		private String search_word;
		private String search_url = "";
		private int Noofpagestoaccess = 1;
		private boolean is_legal = true;
		Link_queue result_links = new Link_queue();

		//public void putin(char search_mode, String search_word) {
		public void putin() {
			search_mode = 'B'; 
			//System.out.println("请输入搜索引擎代码（百度B，谷歌G）")
			//(char)System.in.read();
			if (((int) search_mode >= 97) && ((int) search_mode <= 122))
				search_mode -= 32;
			// System.out.println(search_mode);

			search_word = "C99新特性";
			BufferedReader search_word_reader = new BufferedReader(
					new InputStreamReader(System.in));
			//System.out.println("请输入搜索关键词");
			//search_word_reader.readLine();
			assert(!(search_word.equals("")));
			if((search_word.equals(""))){System.out.println("空搜索关键词");return;}
			
			Noofpagestoaccess = 0;
			assert(Noofpagestoaccess > 0);
			return;
		}

		public void choose_engine_search_word() throws UnsupportedEncodingException{
			
			//putin(search_mode, search_word);
			Set<String> result_links = new TreeSet<String>();
			do {
				putin();
				System.out.println(search_word);
				try {
					if(search_word == "") throw new Exception();
					switch (search_mode) {
					case 'B':
						// 百度
						search_url = "http://www.baidu.com/s?wd="
								+ java.net.URLEncoder
										.encode(search_word, "gbk");

						// DownLoadFile.downloadFile(search_url, "百度下载网页_" +
						// search_word + ".html");
						break;
					case 'G':
						// google
						search_url = "http://www.google.com.hk/search?q="
								+ java.net.URLEncoder.encode(search_word,
										"utf-8");
						System.out.println(search_url);
						// downLoader.downloadFile(search_url,"google下载网页_" +
						// search_word + ".html");
						break;
					default:
						//Exception illegal_Search_mode = new Exception();
						//throw illegal_Search_mode;
						throw new Exception();
						// illegal_Search_mode;
					}
				} catch (Exception illegal_Search_mode) {
					illegal_Search_mode.printStackTrace();
					System.out.println("illegal Search mode");
					is_legal = false;
				};
			} while (is_legal = false);
			
			return;
		}
		
		public void access_to_appointed_page () throws ParserException{
			assert(Noofpagestoaccess > 0);
			switch (search_mode) {
			case 'B':
				// 百度
				search_url = search_url + "&pn=" + (Noofpagestoaccess-1) + "0";
				System.out.println(search_url);
				extractLinks(search_url, search_mode);
				break;
			case 'G':
				// google
				search_url = search_url + "&start=" + (Noofpagestoaccess-1) + "0";
				System.out.println(search_url);
				extractLinks(search_url, search_mode);
				break;
			default:
			}
		}
		
		
			
		public Link_queue get_result_links(){
			return result_links;
		}
		
		public void extractLinks(String url,char search_mode/*,Set<String> result_links*/) {
			//Set<String> result_links = new HashSet<String>();
			try {
				Parser parser = new Parser();
				
				parser.setEncoding("utf-8");

				parser.getConnectionManager().setProxyHost("127.0.0.1");
				parser.getConnectionManager().setProxyPort(8118);
				
				parser.setURL(url);
				
				switch (search_mode) {
				case 'B':// 百度
					//parser.setEncoding("utf-8");
					NodeFilter result_filter_regu = new HasAttributeFilter("class", "result");
					NodeFilter result_filter_op = new HasAttributeFilter("class", "result-op");
					OrFilter result_filter = new OrFilter(result_filter_regu,result_filter_op);
					NodeFilter linkclass_t = new HasAttributeFilter("class","t");
					NodeFilter result_child_filter = new HasParentFilter(result_filter, true);
					AndFilter result_link_filter = new AndFilter(linkclass_t,result_child_filter);

					NodeList nodes = parser.extractAllNodesThatMatch(linkclass_t);
					// NodeList nodes =
					// parser.extractAllNodesThatMatch(result_filter);

					System.out.println("it's test_baidu");
					if (nodes != null) {
						for (int i = 0; i < nodes.size(); ++i) {
							// 逐个取出符合条件的链接结点
							Node textnode = (Node) nodes.elementAt(i);

							// 在链接结点中取得实际有效的链接结点
							Node effective_tag = textnode.getFirstChild();
							
							// 创建一个结果链接存储结构
							Result_Link_Struct result_link_struct = new Result_Link_Struct();
							//result_links.create_new_link();
							
							
							// 抓取有效链接的链接标题
						//	System.out.println(effective_tag.toPlainTextString());
							result_link_struct.link_title = effective_tag.toPlainTextString();
							//result_links.get_last_link().link_title = effective_tag.toPlainTextString();

							// 抓取有效链接的URL
							LinkTag effective_linktag = (LinkTag) effective_tag;
							result_link_struct.link_url = effective_linktag.getLink();
							//result_links.get_last_link().link_url = effective_linktag.getLink();
						//	System.out.println(result_link_struct.link_url);
							
							// 抓取每个有效连接的描述性文字
							// CAUTION!!! //此处（在百度中）对描述性文字的抓取尚有问题
							String describe_text = textnode.getParent()
									.toPlainTextString();
						//	System.out.println(describe_text);
							result_link_struct.link_abstract = describe_text;
							//result_links.get_last_link().link_abstract = describe_text;
							
							result_links.add_link(result_link_struct);
						//	System.out.println("******==================******");
						}
					}
					break;

				case 'G':// google
					//parser.setEncoding("gb2312");
					
					//DownLoadFile downloadhtml;
					//downloadhtml.downloadFile(url);
					
					
					NodeFilter linkclass_r = new HasAttributeFilter("class","r");//用于google的链接结点过滤
					NodeFilter linkclass_st = new HasAttributeFilter("class","st");//用于google的链接描述文字结点过滤
					/*NodeFilter result_child_filter = new HasParentFilter(
							result_filter, true);
					AndFilter result_link_filter = new AndFilter(linkclass_t,
							result_child_filter);
					*/
					NodeList google_nodes_link = parser
							.extractAllNodesThatMatch(linkclass_r);
					NodeList google_nodes_describe = parser.extractAllNodesThatMatch(linkclass_st);

					System.out.println("it's test_google");
					if (google_nodes_link != null) {
						System.out.println("have" + google_nodes_link.size());
						for (int i = 0; i < google_nodes_link.size(); ++i) {
							// 逐个取出符合条件的链接结点
							Node textnode = (Node) google_nodes_link.elementAt(i);

							// 在链接结点中取得实际有效的链接结点
							Node effective_tag = textnode.getFirstChild();
							
							// 创建一个结果链接存储结构
							Result_Link_Struct result_link_struct = new Result_Link_Struct();

							// 抓取有效链接的链接标题
						//	System.out.println(effective_tag.toPlainTextString());
							result_link_struct.link_title = effective_tag.toPlainTextString();
							
							// 抓取有效链接的URL
							LinkTag effective_linktag = (LinkTag) effective_tag;
							result_link_struct.link_url = effective_linktag.getLink();
						//	System.out.println(result_link_struct.link_url);
							
							// 抓取每个有效连接的描述性文字
							// CAUTION!!! //此处（在百度中）对描述性文字的抓取尚有问题
							String describe_text = textnode.getParent().toPlainTextString();
						//	System.out.println(describe_text);
							result_link_struct.link_abstract = describe_text;
							
							result_links.add_link(result_link_struct);
						//	System.out.println("******==================******");
						}
					}
					break;
				default:
					System.out.println("errorrrrrrrrr");
				}

			} catch (ParserException e) {
				e.printStackTrace();
			}
			// return result_links;
			return;
		}
	}

	public class Pages_analysis{
		public void analyze_pages(Set<String> urls_to_analyze){
			for(String url_to_analyze:urls_to_analyze){
				get_page_title(url_to_analyze);
			}
			
			return;
		}
		
		public String get_page_title(String url_to_analyze) {
			String title_analyzed = "";
			try {
				Parser parser = new Parser(url_to_analyze);
				NodeFilter title_filter = new TagNameFilter("title");
				
				parser.getConnectionManager().setProxyHost("127.0.0.1");
				parser.getConnectionManager().setProxyPort(8118);
				// parser.setEncoding(str1);
				try {
					NodeList titles = parser.extractAllNodesThatMatch(title_filter);
					if (titles != null) {
						if (titles.size() == 1) {
							Node title_text = (Node) titles.elementAt(0);
							title_analyzed = title_text.toPlainTextString();
							System.out.println(title_analyzed);
							System.out.println("=================heihei=============");
						} else {
							System.out.println("why is title not exclusive?");
							System.out.println("=================heihei=============");
						}
					}
				} catch (EncodingChangeException e) {
					String encode = e.toString().split("to ")[1];
					encode = encode.split(" at")[0];
					//System.out.println(encode);
					parser.setEncoding(encode);
					NodeList titles = parser.extractAllNodesThatMatch(title_filter);
					if (titles != null) {
						if (titles.size() == 1) {
							Node title_text = (Node) titles.elementAt(0);
							title_analyzed = title_text.toPlainTextString();
							System.out.println(title_analyzed);
							System.out.println("=================heihei=============");
						} else {
							System.out.println("why is title not exclusive?");
							System.out.println("=================heihei=============");
						}
					}
				}

			} catch (ParserException e) {
				e.printStackTrace();
			}
			return title_analyzed;
		}

		public String get_page_mainbody(String url_to_analyze) {
			String mainbody_analyzed = "";
			try {
				Parser parser = new Parser(url_to_analyze);
				NodeFilter mainbody_filter = new HasAttributeFilter("class",
						"articalContent  ");
				NodeList main_body = parser
						.extractAllNodesThatMatch(mainbody_filter);

				if (main_body != null) {
					System.out.println("main_body.size ===== "
							+ main_body.size());
					for (int i = 0; i < main_body.size(); ++i) {
						Node mainbody_text = (Node) main_body.elementAt(0);
						mainbody_analyzed = mainbody_text.toPlainTextString()
								.replace("&nbsp;", " ");
						System.out.println(mainbody_analyzed);
						System.out
								.println("=================heihei=============");
					}
					// else{
					// System.out.println("why is title not exclusive?");
					// }
				}

			} catch (ParserException e) {
				e.printStackTrace();
			}
			return mainbody_analyzed;
		}
	}
	
	public static void main(String[] args) throws HttpException, IOException, ParserException {
		Search_reduplication_removing testtest = new Search_reduplication_removing();
		DownLoadFile downLoader=testtest.new DownLoadFile(); 
		HtmlParserTool htmlparsertool = testtest.new HtmlParserTool();
		
		Link_queue result_links;
		
		htmlparsertool.putin();
		htmlparsertool.choose_engine_search_word();
		htmlparsertool.access_to_appointed_page();
		
		
		result_links = htmlparsertool.get_result_links();
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