package datapackage;

/**
 * 是用以表示每一个链接信息的信息块<br/>
 * 包含链接的一些基本信息<br/>
 * &emsp;比如 链接标题 URL 摘要 及 正文<br/>
 * 还包含一些与聚类相关的信息<br/>
 * &emsp;如 其来自原搜索结果的第几页 及 聚合后所在的组
 * @author Daniel Qian
 * @version 1.0
 */
public class Result_Link_Struct {
/*
 * 类的各数据变量
 */
	/**链接标题 (String)*/
	private String link_title;
	/**链接URL (String)*/
	private String link_url;
	/**链接摘要 (String)*/
	private String link_abstract;
	/**链接正文 (String)*/
	private String link_text;
	/**链接来自原第几个搜索结果页面 (int)*/
	private int link_page_from;
	/**聚合后所在的组*/
	int cluster_id;
	
/*
 * 类方法函数
 */
	/**
	 * 将该链接信息块的信息 输出至控制台
	 */
	public void output(){
		System.out.println("链接标题： " + getLink_title());
		System.out.println("链接url： " + getLink_url());
		System.out.println("链接摘要： " + getLink_abstract());
		System.out.println("链接正文： " + getLink_text());
		System.out.println("链接所在页码： " + getLink_page_from());
		System.out.println("******************************************************************************************");
	}

	/*
	 * 对信息块的各个信息条目进行获取(get)及设置(set)的函数
	 */
	//////////对于set操作 如何保证其私有性？
	public String getLink_title() {
		return link_title;
	}
	public void setLink_title(String link_title) {
		this.link_title = link_title;
	}
	
	public String getLink_url() {
		return link_url;
	}
	public void setLink_url(String link_url) {
		this.link_url = link_url;
	}
	
	public String getLink_abstract() {
		return link_abstract;
	}
	public void setLink_abstract(String link_abstract) {
		this.link_abstract = link_abstract;
	}

	public int getLink_page_from() {
		return link_page_from;
	}
	public void setLink_page_from(int link_page_from) {
		this.link_page_from = link_page_from;
	}

	public String getLink_text() {
		return link_text;
	}
	public void setLink_text(String link_text) {
		this.link_text = link_text;
	}
}
