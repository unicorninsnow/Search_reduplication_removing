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
		//String s = "����һ���������ӷִʵģ��򵥲��ԣ�������";
		String s = "This is a,test for,seperator!Can it work?";
		//splited_strings = s.split("��|��|��|��|\\.|,|\\?|!");  //Ӣ�ĵľ�ź��ʺ���Ҫת��
	}*/
	private static List<String> list = new ArrayList<String>();
	public static void main(String[] args) throws Exception
	{
		InputStreamReader isr = new InputStreamReader(new FileInputStream(new File("D:\\test folder\\clustertest2.txt")),Charset.defaultCharset());
		BufferedReader br = new BufferedReader(isr);
		//identity_judge();		
		String punc_regex = "[��������.,?!]";		//ƥ����һ���ŵ�������ʽ
		Pattern punc_pat = Pattern.compile(punc_regex);	//from java.util.regex������һ�����������regex��pattern
		String line = null;
		while ((line = br.readLine())!= null)
		{
			Matcher punc_mat = punc_pat.matcher(line);	//from java.util.regex���ö����line����Matcher
			String[] substrs = punc_pat.split(line);
			if (substrs.length>0)
			{
				int count = 0;
				while (count < substrs.length)
				{
					if(punc_mat.find())		//ƥ����һ�����
						substrs[count] += punc_mat.group();		//��������ԭ����
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
		
		//������ɿո��滻
	}
}
