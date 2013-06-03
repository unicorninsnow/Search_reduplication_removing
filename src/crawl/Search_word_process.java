package crawl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.TreeSet;

import org.htmlparser.util.ParserException;

import datapackage.Link_queue;
import crawl.Search_engine_process;

public class Search_word_process {
	private char search_mode = 'B';//默认以百度作为搜索引擎
	private String search_word;
	private String search_url = "";
	private int Noofpagetoaccess = 1;
	private boolean is_legal = true;
	
	
	//public void putin(char search_mode, String search_word) {
	public void putin() {
		search_mode = 'B'; 
		//System.out.println("请输入搜索引擎代码（百度B，谷歌G）")
		//(char)System.in.read();
		if (((int) search_mode >= 97) && ((int) search_mode <= 122))
			search_mode -= 32;
		// System.out.println(search_mode);

		search_word = "新时代的机器学习";
		BufferedReader search_word_reader = new BufferedReader(
				new InputStreamReader(System.in));
		//System.out.println("请输入搜索关键词");
		//search_word_reader.readLine();
		assert(!(search_word.equals("")));
		if((search_word.equals(""))){System.out.println("空搜索关键词");return;}
		
		Noofpagetoaccess = 2;
		assert(Noofpagetoaccess > 0);
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
		assert(Noofpagetoaccess > 0);
		switch (search_mode) {
		case 'B':
			// 百度
			search_url = search_url + "&pn=" + (Noofpagetoaccess-1) + "0";
			System.out.println(search_url);
			//Search_engine_process.extractLinks(search_url, search_mode);
			break;
		case 'G':
			// google
			search_url = search_url + "&start=" + (Noofpagetoaccess-1) + "0";
			System.out.println(search_url);
			//Search_engine_process.extractLinks(search_url, search_mode);
			break;
		default:
		}
	}
	
	public char getsearch_mode() {
		return search_mode;
	}
	
	public String getsearch_url() {
		return search_url;
	}
	
	public int getNoofpagetoaccess() {
		return Noofpagetoaccess;
	}
}
