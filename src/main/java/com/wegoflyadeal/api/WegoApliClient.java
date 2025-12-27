package com.wegoflyadeal.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wegoflyadeal.constants.Constants;
import com.wegoflyadeal.helpers.PostApiRequest;
import com.wegoflyadeal.helpers.WegoFlights;

public class WegoApliClient {

	public static CloseableHttpResponse get(String uri) throws ClientProtocolException, IOException
	{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet  httpget = new HttpGet(uri);
		CloseableHttpResponse closeablehttpresponse = httpClient.execute(httpget);
		return closeablehttpresponse;
	}

	public static CloseableHttpResponse get(String uri, HashMap<String, String> headers) throws ClientProtocolException, IOException
	{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet  httpget = new HttpGet(uri);
		for(Map.Entry<String, String> entry : headers.entrySet())
		{
			httpget.addHeader(entry.getKey(),entry.getValue());
		}
		CloseableHttpResponse closeablehttpresponse = httpClient.execute(httpget);
		return closeablehttpresponse;
	}

	public static void postCall(List<WegoFlights> WegoFlightsVar)
	{
		CloseableHttpClient client = null; 
		try { 
			client = HttpClients.createDefault(); 
			HttpPost httpPost = new HttpPost(Constants.Post_API_PATH);

			ObjectMapper om = new ObjectMapper();
			PostApiRequest saudiRailSRPObject = new PostApiRequest(WegoFlightsVar);
			List<PostApiRequest> list = new ArrayList<PostApiRequest>();
			list.add(saudiRailSRPObject);
			String marshalling = om.writeValueAsString(list);
			//System.out.println("Marshaling : "+marshalling);
			int lengthOfString = marshalling.length();
			String updatedJsonString = marshalling.substring(1,(lengthOfString-1));
			//System.out.println("Post API Data : "+updatedJsonString);
			String one = updatedJsonString.substring(15).trim();
			int length = one.length();
			//System.out.println("Without Array Name - API Data : "+one);
			String finalApiData = one.substring(0,length-1).trim();
			System.out.println("API : "+finalApiData);
			httpPost.setEntity(new StringEntity(finalApiData));
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			for(Map.Entry<String,String> header : headers.entrySet()) {
				httpPost.addHeader(header.getKey(),header.getValue()); }

			CloseableHttpResponse response = client.execute(httpPost);
			System.out.println(response.getStatusLine().getStatusCode());
			list.clear();
		}catch(Exception e) { e.printStackTrace(); } 
		finally 
		{ 
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
}
