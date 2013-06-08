package crawl;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import datapackage.Result_Link_Struct;


public class Page_thread implements Runnable{
	final int text_para_threshold = 35;
	Result_Link_Struct page_to_analyze;
	
	public Page_thread(Result_Link_Struct page_tobe_analyze){
		page_to_analyze = page_tobe_analyze;
	}
	
	@Override
	public void run(){
		// TODO 自动生成的方法存根
		String mainbody_analyzed = "";
		//System.out.println(page_to_analyze.getLink_title());
		//System.out.println(page_to_analyze.getLink_url());
		try {
			Parser parser = new Parser(page_to_analyze.getLink_url());

			NodeFilter mainbody_filter = new TagNameFilter("p");
			// NodeFilter mainbody_filter_p = new TagNameFilter("p");
			// NodeFilter mainbody_filter_article = new
			// HasAttributeFilter("class", "article");
			// NodeFilter mainbody_filter = new
			// OrFilter(mainbody_filter_p,mainbody_filter_article);
			try {
				NodeList main_body = parser
						.extractAllNodesThatMatch(mainbody_filter);
				page_to_analyze.setLink_url(parser.getURL());
				if (main_body != null) {
					// System.out.println("main_body.size ===== " +
					// main_body.size());
					for (int i = 0; i < main_body.size(); ++i) {
						Node mainbody_text = (Node) main_body.elementAt(i);
						String temptext = mainbody_text.toPlainTextString()
								.replace("&ldquo;", "“")
								.replace("&rdquo;", "”")
								.replace("&middot;", "·")
								.replace("&nbsp", " ")
								.replace(" ", "").replace("	", "");
						if (temptext.length() > text_para_threshold) {
							mainbody_analyzed = mainbody_analyzed + temptext + "\n";
							// System.out.println(temptext);
						}
						// System.out.println("=================paragraph=============");
					}
					//System.out.println(mainbody_analyzed);
					//System.out.println("=================one page is finished.==================");
					page_to_analyze.setLink_text(mainbody_analyzed);
				} else {
					System.out.println("why is title not exclusive?");
				}
			} catch (EncodingChangeException e) {
				String encode = e.toString().split("to ")[1];
				encode = encode.split(" at")[0];
				// System.out.println(encode);
				parser.setEncoding(encode);
				NodeList main_body = parser
						.extractAllNodesThatMatch(mainbody_filter);
				//String urlString = parser.getURL();
				//System.out.println(urlString);
				//System.out.println(page_to_analyze.getLink_url());
				//page_to_analyze.setLink_url(urlString);
				//System.out.println(page_to_analyze.getLink_url());
				page_to_analyze.setLink_url(parser.getURL());
				if (main_body != null) {
					// System.out.println("main_body.size ===== " +
					// main_body.size());
					for (int i = 0; i < main_body.size(); ++i) {
						Node mainbody_text = (Node) main_body.elementAt(i);
						String temptext = mainbody_text.toPlainTextString()
								.replace("&ldquo;", "“")
								.replace("&rdquo;", "”")
								.replace("&middot;", "·")
								.replace("&nbsp", " ")
								.replace(" ", "").replace("	", "");
						if (temptext.length() > text_para_threshold) {
							mainbody_analyzed = mainbody_analyzed + temptext + "\n";
							// System.out.println(temptext);
						}
						// System.out.println("=================paragraph=============");
					}
					//System.out.println(mainbody_analyzed);
					//System.out.println("=================one page is finished.==================");
					page_to_analyze.setLink_text(mainbody_analyzed);
				} else {
					System.out.println("why is title not exclusive?");
				}
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return;
	}
	
}
