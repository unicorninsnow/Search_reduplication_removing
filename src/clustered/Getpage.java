package clustered;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Getpage {
	public static String getPage(String page) {
		try {

			URL url = new URL(page);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// 以下是修正Server returned HTTP response code: 403 for URL的代码
			// 通常是因为服务器的安全设置不接受Java程序作为客户端访问，解决方案是设置客户端的User Agent
			con.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 5.0;Windows NT; DigExt)");

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					con.getInputStream(),"UTF-8"));

			StringBuilder b = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				b.append(line);
				b.append("\r\n");
			}
			return b.toString();
		} catch (FileNotFoundException ex) {
			System.out.println("NOT FOUND:" + page);
			return null;
		} catch (ConnectException ex) {
			System.out.println("Timeout:" + page);
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
