package com.kmk.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class MyHttpClient {
	public String requestGet(String uri) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(uri);
		try {
			CloseableHttpResponse response;
			response = client.execute(request);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				reponseStr.append(line);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponseStr.toString();
	}

	public String requestPostForm(String uri, List<NameValuePair> params) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(uri);
		try {
			request.setHeader("Content-type", "multipart/form-data");
			request.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse response;
			response = client.execute(request);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				reponseStr.append(line);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponseStr.toString();
	}

	public String requestPostJson(String uri, String json) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(uri);
		try {
			request.setHeader("Content-type", "application/json");
			request.setEntity(new StringEntity(json));
			CloseableHttpResponse response;
			response = client.execute(request);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				reponseStr.append(line);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponseStr.toString();
	}

	public String requestPostString(String uri, String str) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(uri);
		try {
			request.setHeader("Content-type", "text/plain");
			request.setEntity(new StringEntity(str));
			CloseableHttpResponse response;
			response = client.execute(request);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				reponseStr.append(line);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponseStr.toString();
	}

	public String requestPutJson(String uri, String json) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPut request = new HttpPut(uri);
		try {
			request.setEntity(new StringEntity(json));
			request.setHeader("Content-type", "application/json");
			CloseableHttpResponse response;
			response = client.execute(request);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				reponseStr.append(line);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponseStr.toString();
	}

	public String requestDelete(String uri) {
		StringBuffer reponseStr = new StringBuffer();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpDelete request = new HttpDelete(uri);
		try {
			CloseableHttpResponse response;
			response = client.execute(request);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				reponseStr.append(line);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponseStr.toString();
	}
}
