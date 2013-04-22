import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Similarity_Judgement {

	/*public static void identity_judge()
	{
		
	}*/
	//public static String[] splited_strings = new String[20];
	/*public static void split_test()
	{
		//String s = "这是一个，按句子分词的，简单测试！可以吗？";
		String s = "This is a,test for,seperator!Can it work?";
		//splited_strings = s.split("。|？|！|，|\\.|,|\\?|!");  //英文的句号和问号需要转义
	}*/
	private static List<String> list = new ArrayList<String>();
	public static void main(String[] args) throws Exception
	{
		InputStreamReader isr = new InputStreamReader(new FileInputStream(new File("D:\\test folder\\clustertest2.txt")),Charset.defaultCharset());
		BufferedReader br = new BufferedReader(isr);
		//identity_judge();		
		String punc_regex = "[。？！，.,?!]";		//匹配任一符号的正则表达式
		Pattern punc_pat = Pattern.compile(punc_regex);	//from java.util.regex，创建一个基于上面的regex的pattern
		String line = null;
		while ((line = br.readLine())!= null)
		{
			Matcher punc_mat = punc_pat.matcher(line);	//from java.util.regex，用读入的line创建Matcher
			String[] substrs = punc_pat.split(line);
			if (substrs.length>0)
			{
				int count = 0;
				while (count < substrs.length)
				{
					if(punc_mat.find())		//匹配下一个标点
						substrs[count] += punc_mat.group();		//将标点加入原句中
					list.add(substrs[count]);
					count++;
				}
			}
		}
		br.close();
		isr.close();
		for (Iterator i = list.iterator(); i.hasNext();) 
			System.out.println(i.next());
		//split_test();
		/*for (int i = 0; i<splited_strings.length; i++)
			System.out.println(splited_strings[i]);*/
		
		//还需完成空格替换
	}
}
