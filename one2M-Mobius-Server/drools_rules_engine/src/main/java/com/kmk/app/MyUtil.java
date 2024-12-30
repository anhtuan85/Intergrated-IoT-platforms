package com.kmk.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtil {

	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String str = sdf.format(new Date());
		return str;
	}

	public static String getBufferedString(String filename) {
		StringBuffer stringBuffer = new StringBuffer();
		if (new File(filename).exists()) {
			try {
				FileReader reader = new FileReader(filename);
				BufferedReader bufferedReader = new BufferedReader(reader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringBuffer.append(line + "\n");
				}
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuffer.toString();
	}

	public static void setBufferedString(String filename, String filecontent) {
		try {
			FileWriter writer = new FileWriter(filename, false);// update(IF true THEN append)
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write(filecontent);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
