﻿package similarity_judge;
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
	private static int SENTENCE_MINLEN = 5;		//作为句子存储的最小长度
	private static double JUDGE_RATIO = 0.4;	//判断相似的阈值
	private static int MAX_HAMMING_DIS = 3;	//文本相似的hamming距离
	private static double TITLE_RATIO = 0.6;	//判断标题是否相似的比例
	
	/* 相似度判断 */
	public static boolean similarity_judge(String article1, String article2, int algo_choice) throws IOException
	{
		List<String> list1 = new ArrayList<String>();	
		List<String> list2 = new ArrayList<String>();	//存储两篇文章分句的结果list
		boolean is_similar = false;
		if (algo_choice == 1)	//选择最简单的判重算法 
		{
			list1.removeAll(list1);
			list2.removeAll(list2);
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
			short[] fp1 = new short[64], fp2 =  new short[64];
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
		
	//两个finggerprint的hamming距离
	public static int get_distance(short[] fp1, short[] fp2)
	{
		int dis = 0;
		for (int i = 0; i<64; i++)
			if (fp1[i] != fp2[i]) dis ++;
		return dis;
	}

	//利用最长公共子序列判断两个标题是否相似
	public static boolean title_judge(String title1, String title2)
	{
		char[] ch1 = title1.toCharArray();
		char[] ch2 = title2.toCharArray();
		//System.out.println(ch1);
		//System.out.println(ch2);
		int len1 = ch1.length, len2 = ch2.length;
		//System.out.println(ch1[0]);
		//System.out.println(ch2[0]);
		int[][] lcs = new int [len1 + 1][len2 + 1];
		for (int i = 0; i <= len1; i++)
			for (int j = 0; j <= len2; j++)
				lcs[i][j] = 0;
			
		//动态规划求最长子序列的长度
		for (int i = 1; i <= len1; i++)
			for (int j = 1; j <= len2; j++)
			{
					if (ch1[i - 1] == ch2[j - 1]) 
						lcs[i][j] = lcs[i-1][j-1] + 1;
					else
						lcs[i][j] = lcs[i][j - 1] > lcs[i - 1][j] ? lcs[i][j - 1] : lcs[i - 1][j];
			}
		
		double maxlen  = lcs[len1][len2];
		//System.out.println(maxlen);
		double ratio  = (maxlen/len1 + maxlen/len2) / 2; 	//用占两个字符串比例的平均值作为衡量标准
		//System.out.println(ratio);
		if (ratio > TITLE_RATIO) return true;
		else return false;
	}
	
	//得到一篇文章对应的fingerprint
	public static short[] getfp(Reader article1, Seg seg) throws IOException
	{
		short[] fp = new short[64];
		MMSeg mmseg = new MMSeg(article1, seg);
		Word word;
		
		while ((word = mmseg.next()) != null)
		{
			//用混合hash方法得到64位hash值
			String str = word.getString();
			long hash = str.hashCode();   
		     hash <<= 32;   
		     hash |= FNVHash(str);   
			//System.out.println(str);
			//System.out.println(hash);
			char[] bichar = tobinary(hash);
			//String bistr = Integer.toBinaryString(hashcode); 
			//System.out.println(bichar);
			for (int i = 0; i < 64; i++)
			{
				if (bichar[i] == '1') fp[i]++;
				else fp[i]--;
			}
		}
		
		for (int i = 0; i<64; i++)
			if (fp[i]>= 0) 
			{
				fp[i] = 1;
				//System.out.println("yes");
			}
			else 
			{
				fp[i] = 0;
				//System.out.println("no");
			}
		System.out.println(fp);
		return fp;
	}
	
	//将一个数转换为对应二进制数的字符串
	public static char[] tobinary(long x)
	{
		char[] bichar = new char[64];
        for (int i = 63; i >= 0; i--) {
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
	
	public static int FNVHash(String data)   
    {   
        final int p = 16777619;   
        int hash = (int)2166136261L;   
        for(int i = 0; i < data.length(); i++)   
            hash = (hash ^ data.charAt(i)) * p;   
        hash += hash << 13;   
        hash ^= hash >> 7;   
        hash += hash << 3;   
        hash ^= hash >> 17;   
        hash += hash << 5;   
        return hash;   
    }
	
	public static void main(String[] args) throws Exception
	{
		//String str1 = strfromfile("D:\\test folder\\clustertest1.txt");
		//String str2 = strfromfile("D:\\test folder\\clustertest2.txt");
		//String str3 = strfromfile("D:\\test folder\\clustertest3.txt");
		//String str4 = strfromfile("D:\\test folder\\clustertest4.txt");
		//Boolean is_similar = similarity_judge(str1, str2);
		//Boolean is_similar = similarity_judge(str1, str3);
		//Boolean is_similar1 = similarity_judge(str1, str3, 2);
		//Boolean is_similar2 = similarity_judge(str1, str4, 2);
		//String s1 = "新时代的机器学习";
		//String s2 = "新时期的机器学习";
		//Boolean b = title_judge(s1, s2);
		//System.out.println(b);
		//System.out.println(is_similar1);
		//System.out.println(is_similar2);	
	}
}
