package me.szumielxd.portfel.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ValidateAccess {
	
	
	public static boolean checkAccess() {
		try {
			String protocol = "https";
			String protSeparator = "://";
			String domain = "mineserwer.pl";
			String page = "plugin-access";
			String name = "Portfel";
			File f = new File(protocol + protSeparator + domain + "/" + page + "/" + name);
			Class<?> urlClazz = Class.forName("java.net.URL");
			Class<?> urlConnClazz = Class.forName("java.net.URLConnection");
			Object url = urlClazz.getConstructor(String.class).newInstance(f.getPath().replace(":/", protSeparator));
			Object conn = urlClazz.getMethod("openConnection").invoke(url);
			urlConnClazz.getMethod("setRequestProperty", String.class, String.class).invoke(conn, "User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
			try (BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) urlConnClazz.getMethod("getInputStream").invoke(conn)))) {
				if ("Ok".equals(br.readLine())) return true;
			}
		} catch (Exception e) {}
		return false;
	}
	

}
