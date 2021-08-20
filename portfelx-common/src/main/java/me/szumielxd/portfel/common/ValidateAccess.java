package me.szumielxd.portfel.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ValidateAccess {
	
	
	public static boolean checkAccess() {
		try {
			URL url = new URL("https://mineserwer.pl/plugin-access/Portfel");
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				if ("Ok".equals(br.readLine())) return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	

}
