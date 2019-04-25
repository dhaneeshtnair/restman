package com.restman.core;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TestCaseExecutor
{
	private TestCaseLoader testCaseLoader = new TestCaseLoader();
	

	String p1 = "\\{\\{(.*?)\\}\\}";
	String p2 = "\\$\\{(.*?)\\}";

	Pattern pattern1 = Pattern.compile(p1);
	Pattern pattern2 = Pattern.compile(p2);
	VelocityEngine velocityEngine = new VelocityEngine();

	private static TestCaseExecutor testCaseExecutor = null;

	private TestCaseExecutor()
	{
		velocityEngine.init();
	}

	public static synchronized TestCaseExecutor getInstance()
	{
		if(testCaseExecutor==null)
		{
			synchronized (TestCaseExecutor.class)
			{
				if(testCaseExecutor==null)
				{
					testCaseExecutor = new TestCaseExecutor();
				}
			}
		}
		System.out.println("Creating Instance of TestCaseExecutor");
		return testCaseExecutor;
	}

	public TestCaseLoader getLoader()
	{
		return testCaseLoader;
	}
	public ScriptEngine getEngine()
	{
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");
		return nashorn;
	}
	public static void main1(String[] args)
	{
		HashMap<String, String> contextVariableMap  = new HashMap<>();
		contextVariableMap.put("host", "cms-dev.in.here.com");
		contextVariableMap.put("id", "3317691592");
		TestCaseExecutor testCaseExecutor = new TestCaseExecutor();
		TestCase testCase = testCaseExecutor.execute(testCaseExecutor.getLoader().testCaseById("input2"),contextVariableMap);
		System.out.println(new Gson().toJson(testCase));
		//		Matcher m=te.variablePattern.matcher("http://${host}/v1/transportAccessRestriction/3317691592?userName=1cmstst");
		//		System.out.println(m.find());
		//		System.out.println(m.group(1));
	}
	public TestCase execute(String testFileName,HashMap<String, String> contextVariableMap)
	{
		String content= TestCaseLoader.toString(new File(testFileName));
		return executeData(content,contextVariableMap);
	}
	public TestCase executeData(String content,Map<String, String> contextVariableMap){
		return	executeData(content, contextVariableMap,null);
	}
	public TestCase executeData(String content,Map<String, String> contextVariableMap,JsonObject contextJson)
	{
		JsonElement jsonElement = new JsonParser().parse(replaceVariables(content,contextVariableMap));

		if (jsonElement instanceof JsonObject)
		{
			JsonObject jsonObject = (JsonObject) jsonElement;
			String rawUrl = jsonObject.get("url").getAsString();
			System.out.println("URL: "+rawUrl);
			
			String method = jsonObject.get("method").getAsString();
			System.out.println("Method: "+method);
			
			JsonElement inputJson = jsonObject.get("input");
			System.out.println("Input decoded!");
			JsonArray assertions = jsonObject.get("assertions").getAsJsonArray();
			
			
			
			String contentType =(jsonObject.has("contentType"))?jsonObject.get("contentType").getAsString():"application/json";
			
			TestCaseResult testCaseResult = delegateExecute(rawUrl, method,contentType, inputJson, assertions);
//			if(jsonObject.has("tags")){
//				JsonArray ja = jsonObject.get("tags").getAsJsonArray();
//				List<String> tags = new ArrayList<>();
//				for(int i=0;i<ja.size();i++)
//					tags.add(ja.get(i).getAsString());
//				testCaseResult.setTags(tags);
//			}
			testCaseResult.setName(jsonObject.get("name").getAsString());
			testCaseResult.setTestCaseId(jsonObject.get("id").getAsString());
			testCaseResult.setDescription(jsonObject.has("description")?jsonObject.get("description").getAsString():"");
			
			String onFailure =jsonObject.has("onFailure")? jsonObject.get("onFailure").getAsString():"";
			String onSuccess =jsonObject.has("onSuccess")? jsonObject.get("onSuccess").getAsString():"";
			
			
			
			TestCase testCase = new TestCase(testCaseResult, onSuccess, onFailure);
			int index = jsonObject.get("stepIndex").getAsInt();
			testCase.setIndex(index);
			return testCase;
		}
		else
		{
			throw new RuntimeException("Not implemented for jsonArray currently");
		}
	}
	private Request getRequest(String url,JsonElement input, String type,String contentType)
	{
		switch (type)
		{
		case "POST":
			return Request.Post(url).bodyString(input.toString(), getCtype(contentType));
		case "GET":
			return Request.Get(url);
		case "PUT":
			return Request.Put(url).bodyString(input.toString(), getCtype(contentType));
		case "DELETE":
			return Request.Delete(url).bodyString(input.toString(),getCtype(contentType));
		}
		return Request.Get(url);
	}
	
	private ContentType getCtype(String cType){
		
		if(cType.contains("application/xml")){
			
			return ContentType.APPLICATION_XML;
		}else if(cType.contains("plain") || cType.contains("html")){
			return ContentType.TEXT_HTML;
			
		}else if(cType.contains("application/x-www-form-urlencoded")){
			return ContentType.APPLICATION_FORM_URLENCODED;
			
		}else
			return ContentType.APPLICATION_JSON;
	}
	private TestCaseResult delegateExecute(String url, String method,String contentType, JsonElement input, JsonArray assertions)
	{
		TestCaseResult testCaseResult = new TestCaseResult();
		try
		{
			System.out.println("Executing Request...");
			Response response = getRequest(url.trim(), input, method,contentType).execute();
			System.out.println("Generated Response!");
			System.out.println("Validating Assertions...");
			testCaseResult.setAssertions(validateAssertions(testCaseResult,response,assertions));
			Boolean status = testCaseResult.getAssertions().stream().allMatch(x->x.getStatus().equals(TestCaseResult.Status.SUCCESS));
			if(status)	
			{
				testCaseResult.setStatus(TestCaseResult.Status.SUCCESS);
			}
			else
			{
				testCaseResult.setStatus(TestCaseResult.Status.FAILURE);
			}
			System.out.println("Assertions Validated!");
		}catch (Exception e){
			e.printStackTrace();
			testCaseResult.setStatus(TestCaseResult.Status.FAILURE);
			testCaseResult.setErrorMessage(e.toString());
		}
		return testCaseResult;
	}
	private List<TestCaseResult.TestAssertion> validateAssertions(Response response, JsonArray assertRequests){
		
		return validateAssertions(null, response, assertRequests);
	}
	private List<TestCaseResult.TestAssertion> validateAssertions(TestCaseResult testCaseResult, Response response, JsonArray assertRequests)
	{

		List<TestCaseResult.TestAssertion> assertionResponses = new ArrayList<>();
		ScriptEngine nashorn = getEngine();
		try
		{
			HttpResponse httpResponse = response.returnResponse();
			byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
			nashorn.eval("var responseCode="+httpResponse.getStatusLine().getStatusCode());
			String resp = new String(bytes);
			if(testCaseResult!=null){
				
				try{
					testCaseResult.setResponse(new JsonParser().parse(resp));
				}catch(Exception e){
					e.printStackTrace();
					testCaseResult.setResponse(resp);
				}
			}
			//System.out.println(resp);
			nashorn.eval("var response="+resp+"");
			
			for (int i = 0; i < assertRequests.size(); i++)
			{
				TestCaseResult.TestAssertion testAssertion = new TestCaseResult.TestAssertion();
				JsonObject asserty = assertRequests.get(i).getAsJsonObject();
				testAssertion.setName(asserty.get("name").getAsString());
				System.out.println("name: "+testAssertion.getName());
				testAssertion.setExpect(asserty.get("expect").getAsString());
				System.out.println("assert: "+testAssertion.getExpect());
				try
				{
					Boolean status = (Boolean) nashorn.eval(asserty.get("expect").getAsString());
					if(status){
						testAssertion.setStatus(TestCaseResult.Status.SUCCESS);
						System.out.println("Validation Success");
					}else
					{
						testAssertion.setStatus(TestCaseResult.Status.FAILURE);
						System.out.println("Validation Failure");
					}

					Matcher m = pattern1.matcher(asserty.get("message").getAsString());
					if (m.find())
					{
						System.out.println("Found value: "+m.group(1));
						String em = m.replaceAll(nashorn.eval(m.group(1)).toString());
						testAssertion.setMessage(em);
					}
					else
					{
						System.out.println("NO MATCH");
					}
				}
				catch(Exception e)
				{
					testAssertion.setStatus(TestCaseResult.Status.FAILURE);
					testAssertion.setErrorMessage(e.getMessage());
				}
				assertionResponses.add(testAssertion);
			}
		}
		catch (ScriptException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}finally {
			
		}
		return assertionResponses;
	}

	public String replaceVariables(String input,Map<String, String> contextMap)
	{
		if(contextMap.isEmpty())
		{
			return input;
		}
		StringWriter writer = new StringWriter();
		try
		{
			VelocityContext context = new VelocityContext();
			RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
			StringReader reader = new StringReader(input);
			SimpleNode node = runtimeServices.parse(reader, UUID.randomUUID().toString());
			Template template = new Template();
			template.setRuntimeServices(runtimeServices);
			template.setData(node);
			template.initDocument();
			for(Map.Entry<String, String> entry : contextMap.entrySet())
			{
				context.put(entry.getKey(),entry.getValue());
			}
			template.merge( context, writer );
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			return input;
		}
		return writer.toString();
	}
}