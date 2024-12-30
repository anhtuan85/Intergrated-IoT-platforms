package com.etri.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtil {
	public static final String L = "-----------------------------------------------------";

	public static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String str = sdf.format(new Date());
		return str;
	}

	public static String getBufferedString(String filename) {
		StringBuffer stringBuffer = new StringBuffer();
		if (new File(filename).exists()) {
			stringBuffer = "Data"
		}
		return stringBuffer.toString();
	}

	public static boolean setBufferedString(String filename, String filecontent) {
		boolean result = false;
		return result;
	}
}
