package similarity_judge;
import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;


public class Similarity_Judgement 
{	
	private static List<String> list1 = new ArrayList<String>();	
	private static List<String> list2 = new ArrayList<String>();	//存储两篇文章分句的结果list
	private static int SENTENCE_MINLEN = 5;		//作为句子存储的最小长度
	private static double JUDGE_RATIO = 0.8;	//判断相似的阈值
	private static int MAX_HAMMING_DIS = 2;	//文本相似的hamming距离
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
	public static boolean similarity_judge(String article1, String article2, int algo_choice) throws IOException
	{
		boolean is_similar = false;
		if (algo_choice == 1)	//选择最简单的判重算法 
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
			if (ratio > JUDGE_RATIO) is_similar = true;
			else is_similar = false;
		}
		
		else if(algo_choice == 2)		//选择simhash算法
		{
			Dictionary dic = Dictionary.getInstance();
			Seg seg = new ComplexSeg(dic);
			char[] fp1 = new char[32], fp2 =  new char[64];
			fp1 = getfp(new StringReader(article1), seg);
			fp2 = getfp(new StringReader(article2), seg);
			//System.out.println(fp1);
			//System.out.println(fp2);
			int dis = get_distance(fp1, fp2);
			System.out.println(dis);
			if (dis > MAX_HAMMING_DIS) is_similar =  false;
			else is_similar =  true;
		}
		return is_similar;
	}
	
	public static int get_distance(char[] fp1, char[] fp2)
	{
		int dis = 0;
		for (int i = 0; i<32; i++)
			if (fp1[i] != fp2[i]) dis ++;
		return dis;
	}
	
	public static char[] getfp(Reader article1, Seg seg) throws IOException
	{
		char[] fp = new char[32];
		MMSeg mmseg = new MMSeg(article1, seg);
		Word word;
		
		while ((word = mmseg.next()) != null)
		{
			String str = word.getString();
			int hashcode = str.hashCode();
			//System.out.println(str);
			//System.out.println(hashcode);
			char[] bichar = tobinary(hashcode);
			//String bistr = Integer.toBinaryString(hashcode); 
			//System.out.println(bichar);
			for (int i = 0; i < 32; i++)
			{
				if (bichar[i] == '1') fp[i]++;
				else fp[i]--;
			}
		}
		//System.out.println(fp);
		for (int i = 0; i<32; i++)
			if (fp[i]>= 0) fp[i] = 1;
			else fp[i] = 0;
		
		return fp;
	}
	
	public static char[] tobinary(int x)
	{
		char[] bichar = new char[32];
        for (int i = 31; i >= 0; i--) {
            if ((x & (1 << i)) != 0) {
                bichar[i] = '1';
            } else {
                bichar[i] = '0';
            }
        }
        return bichar;
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
		isr.close();
		
		return buffer.toString();
	}
	
	public static void main(String[] args) throws Exception
	{
		String str1 = strfromfile("test folder\\clustertest1.txt");
		String str2 = strfromfile("test folder\\clustertest2.txt");
		String str3 = strfromfile("test folder\\clustertest3.txt");
		String str4 = strfromfile("test folder\\clustertest4.txt");
		//Boolean is_similar = similarity_judge(str1, str2);
		//Boolean is_similar = similarity_judge(str1, str3);
		Boolean is_similar1 = similarity_judge(str1, str2, 1);
		Boolean is_similar2 = similarity_judge(str1, str4, 1);
		System.out.println(is_similar1);
		System.out.println(is_similar2);	
	}
}
