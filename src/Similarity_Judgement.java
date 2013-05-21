import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Similarity_Judgement 
{	
	private static List<String> list1 = new ArrayList<String>();	
	private static List<String> list2 = new ArrayList<String>();	//存储两篇文章分句的结果list
	private static int SENTENCE_MINLEN = 5;		//作为句子存储的最小长度
	private static double JUDGE_RATIO = 0.8;	//判断相似的阈值
	
	/* 分割以String保存的正文 */
	public static void article_divide(String str,	List<String> list)
	{
		String punc_regex = "[。？！，.,?!\\s]";		// \s代表空格
		Pattern punc_pat = Pattern.compile(punc_regex);	//from java.util.regex，创建一个基于上面的regex的pattern
		Matcher punc_mat = punc_pat.matcher(str);
		String[] substrs = punc_pat.split(str);
		int count = 0;
		while (count < substrs.length)
		{
			if (substrs[count].length() >= SENTENCE_MINLEN)
				list.add(substrs[count]);
			count ++;
		}

		for (Iterator i = list.iterator(); i.hasNext();) 
			System.out.println(i.next());
	}
	
	/* 相似度判断 */
	public static boolean similarity_judge(String article1, String article2)
	{
		article_divide(article1, list1);
		article_divide(article2, list2);
		int all1= list1.size(), all2 = list2.size(), same = 0;
		String str;
		for (int i = 0; i < all2; i++)
		{
			str = list2.get(i);
			if  (list1.contains(str)) same ++;
		}
		double ratio = (double)same / all1; 
		System.out.println(same);
		System.out.println(all1);
		System.out.println(all2);
		System.out.println(ratio);
		if (ratio > JUDGE_RATIO) return true;
		else return false;
	}
	
	/*	从文件中读取文章（用于测试） */
	public static String strfromfile(String file) throws IOException
	{
		InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(file)),Charset.defaultCharset());
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		StringBuffer buffer = new StringBuffer();	//用以存储正文字符串的buffer
		
		while ((line = br.readLine())!= null)
			buffer.append(line);
		
		br.close();
		isr.close();
		
		return buffer.toString();
	}
	
	public static void main(String[] args) throws Exception
	{
		String str1 = strfromfile("D:\\test folder\\clustertest1.txt");
		String str2 = strfromfile("D:\\test folder\\clustertest2.txt");
		String str3 = strfromfile("D:\\test folder\\clustertest3.txt");
		String str4 = strfromfile("D:\\test folder\\clustertest4.txt");
		Boolean is_similar = similarity_judge(str1, str2);
		//Boolean is_similar = similarity_judge(str1, str3);
		//Boolean is_similar = similarity_judge(str1, str4);
		System.out.println(is_similar);
	}
}
