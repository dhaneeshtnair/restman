package com.restman.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.StringEntity;

public class PerfTest {
	
	
	public static void main1(String[] args) {
		
		String json =  "{\n \"accessRestrictions\":[\n    {\n      \"properties\" : {\n    \"dataSource\" : 1234567,\n    \"seasonallyClosed\" : false,\n    \"laneNumbers\" : [ ],\n    \"minPassengers\" : 3,\n    \"dayOfWeekModifiers\" : [ {\n        \"startTime\" : \"0530\",\n        \"endTime\" : \"1230\",\n        \"attributes\" : [ ],\n        \"days\" : [ \"Monday\", \"Tuesday\", \"Wednesday\", \"Thursday\", \"Friday\" ]\n      } ]\n},\n      \"id\": 5665763598\n    },\n    {\n      \"properties\" : {\n    \"seasonallyClosed\" : false,\n      \"dayOfWeekModifiers\" : [ ],\n      \"externalModifiers\" : [ ],\n      \"dateRangesModifiers\" : [ ],\n      \"dayOfWeekOfMonthModifiers\" : [ ],\n      \"dayOfMonthModifiers\" : [ ],\n      \"dayOfWeekOfYearModifiers\" : [ ],\n      \"monthOfYearModifiers\" : [ {\n        \"startTime\" : \"0200\",\n        \"endTime\" : \"2300\",\n        \"attributes\" : [ ],\n        \"startMonth\" : 11,\n        \"endMonth\" : 12\n      }, {\n        \"startTime\" : \"0000\",\n        \"endTime\" : \"2300\",\n        \"attributes\" : [ ],\n        \"startMonth\" : 1,\n        \"endMonth\" : 4\n      } ],\n      \"dayOfMonthOfYearModifiers\" : [ ],\n      \"weekOfMonthModifiers\" : [ ]\n},\n      \"id\": 126490074\n    }\n    ]\n}";
		
		ExecutorService exec  = Executors.newFixedThreadPool(50);
		
		for(int i=0;i<10000;i++){
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					Response res = Request.Put("http://dchivtbat02:55661/accessRestriction/batch/verify?userName=1cmstst").addHeader("Content-Type", "application/json").body(new StringEntity(json)).execute();
					System.out.println(res.returnResponse().getStatusLine().getStatusCode()==200);
				
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		}
		
		exec.shutdown();
		try {
			exec.awaitTermination(100, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
	}

}
