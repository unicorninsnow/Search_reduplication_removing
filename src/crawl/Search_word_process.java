package crawl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.TreeSet;

import org.htmlparser.util.ParserException;

import datapackage.Link_queue;
import crawl.Search_engine_process;

/**
 * 对搜索关键词进行处理的模块的类<br/>
 * 最终得出由搜索关键词和基础搜索引擎所对应的搜索结果URL
 * @author Daniel
 * @version 1.1 将输入函数改为重载的类的构造函数
 */
public class Search_word_process {
	/**基础搜索引擎代号(char)<br/>取值范围 'B' || 'G'*/
	//默认以百度作为搜索引擎
	private char search_mode = 'B';
	/**搜索关键词(String)*/
	private String search_word;
	/**用以存储搜索结果页面的URL(String)*/
	private String search_url = "";
	/**表示需要的是对应关键词的搜索结果第几个结果页面(int >= 0)*/
	private int Noofpagetoaccess = 1;
	/**表示各种输入的数据是否合法的标识符(boolean)*/
	private boolean is_legal = true;
	
	/**
	 * 重载类的构造函数 用以实现 输入函数
	 * 输入搜索关键词 基础搜索引擎 页面号等变量
	 * @param mode 基础搜索引擎代号(char)  取值范围 'B' || 'G'
	 * @param word 搜索关键词(String)
	 * @param numofpage 表示需要的是对应关键词的搜索结果第几个结果页面(int > 0)(从第1页开始计数)
	 * @throws Exception 输入信息非法异常
	 */
	public Search_word_process(char mode,String word,int numofpage) throws Exception{
		/* 传入参数合法性检测 */
		//处理大小写（若为小写全部改为大写）
		if (((int) mode >= 97) && ((int) mode <= 122))	mode -= 32;
		//若基础搜索引擎代码非法（百度B，谷歌G）  则抛出异常
		if((mode != 'B')&&(mode != 'G')) throw new Exception("Illegal search mode!");
		//若搜索关键词为空 则抛出异常
		if(word.equals("")) throw new Exception("null search word!");
		//若搜索页号不为非负整数 则抛出异常
		if(numofpage <=0) throw new Exception("Illegal search page!");
		
		/* 初始化变量 */
		search_mode = mode;
		search_word = word;
		Noofpagetoaccess = numofpage;
	}

	/**
	 * 该方法用于处理搜索关键词 最终生成所需搜索结果页面的URL<br/>
	 * 分不同基础搜索引擎进行 关键词转化为URL格式的代码 并添加页号信息<br/>
	 * 最后将URL存入search_url中
	 * @throws Exception
	 */
	public void handle_search_word_url() throws Exception {
		if (search_word.equals("")) {
			is_legal = false;
			throw new Exception("null search word!");
		}
		if (Noofpagetoaccess <= 0) {
			is_legal = false;
			throw new Exception("Illegal search page!");
		}
		try {
			switch (search_mode) {
			case 'B':
				// 百度
				search_url = "http://www.baidu.com/s?wd="
						+ java.net.URLEncoder.encode(search_word, "gbk")
						+ "&pn=" + (Noofpagetoaccess - 1) + "0";
				break;
			case 'G':
				// 谷歌
				search_url = "http://www.google.com.hk/search?q="
						+ java.net.URLEncoder.encode(search_word, "utf-8")
						+ "&start=" + (Noofpagetoaccess-1) + "0";
				break;
			default:
				is_legal = false;
				throw new Exception("illegal_Search_mode");
			}
		} catch (UnsupportedEncodingException encode_err) {
			encode_err.printStackTrace();
			is_legal = false;
			throw new Exception("Search word is not supported of encoding!");
		}
		return;
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
