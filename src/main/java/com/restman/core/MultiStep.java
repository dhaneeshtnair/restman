package com.restman.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MultiStep
{
	public HashMap<String, String> testCases = new HashMap<String,String>();
	public HashMap<String, TestCase> testCaseResults = new HashMap<String,TestCase>();

	public ArrayList<TestCaseResult> output = new ArrayList<TestCaseResult>();

	public MultiStep(String inputJson)
	{
		JsonElement jsonElement = new JsonParser().parse(inputJson);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();//will return members of your object
		for(Map.Entry<String, JsonElement> entry: entries)
		{
			testCases.put(entry.getKey().toString(), entry.getValue().toString());
		}
		TestCase testCase;
		String nextStep = "input1";
		while(!nextStep.equalsIgnoreCase("None"))
		{
			testCase = TestCaseExecutor.getInstance().executeData(testCases.get(nextStep),new HashMap<>());
			testCaseResults.put(testCase.getTestCaseResult().getTestCaseId(),testCase);
			if(testCase.getTestCaseResult().getStatus()== TestCaseResult.Status.SUCCESS)
			{
				nextStep = testCase.getOnSuccess().toString();
				testCase.getTestCaseResult().setNextStep(nextStep);
				//System.out.println(testCase.getTestCaseResult().getTestCaseId()+" succesful, now executing "+nextStep);
				//System.out.println("#################################################################################");
			}
			else if(testCase.getTestCaseResult().getStatus()== TestCaseResult.Status.FAILURE)
			{
				nextStep = testCase.getOnFailure().toString();
				testCase.getTestCaseResult().setNextStep(nextStep);
				//System.out.println(testCase.getTestCaseResult().getTestCaseId()+" failed, now executing "+nextStep);
				//System.out.println("#################################################################################");
			}
		}
		for(String key: testCaseResults.keySet())
		{
			output.add(testCaseResults.get(key).getTestCaseResult());
		}
		Collections.reverse(output);
	}
}