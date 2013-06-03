package crawl;

import java.util.Set;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class Pages_analysis {
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
