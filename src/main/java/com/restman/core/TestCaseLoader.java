package com.restman.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("unused")
public class TestCaseLoader
{
	private static Map<String, String> testCaseIdMap = new ConcurrentHashMap<>(); 
	private static Map<String, List<String>> tagMap = new ConcurrentHashMap<>(); 
	private JsonParser parser = new JsonParser();

	public TestCaseLoader()
	{
		loadAllTests();
	}
	public void loadAllTests()
	{
//		File files[] = new File("src/main/resources/testcase").listFiles();
//
//		Arrays.asList(files).parallelStream().forEach(x->{
//			String jsonFileString = toString(x);
//			JsonObject jsonObject = parser.parse(jsonFileString).getAsJsonObject();
//			testCaseIdMap.put( jsonObject.get("id").getAsString(), x.getPath() );
//			JsonArray jsonArray = jsonObject.get("tags").getAsJsonArray();
//			for(int i=0;i<jsonArray.size();i++)
//			{
//				List<String> tests = tagMap.get(jsonArray.get(i).getAsString());
//				if(tests == null)
//				{
//					tests = new ArrayList<>();
//					tests.add(x.getPath());
//				}
//				tagMap.put(jsonArray.get(i).getAsString(), tests);
//			}
//		});
	}
//	public static void main(String[] args)
//	{
//		TestCaseLoader testCaseLoader =new TestCaseLoader();
//	}

	public static String toString(File f)
	{
		StringBuffer sb = new StringBuffer();
		try(InputStream fis = new FileInputStream(f))
		{
			BufferedReader bfr =new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while((line=bfr.readLine())!=null)
			{
				sb.append(line);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
	public String testCaseById(String testCaseId)
	{
		return testCaseIdMap.get(testCaseId);
	}
	public List<String> testCaseByTagName(String tagName)
	{
		return tagMap.get(tagName);
	}
}