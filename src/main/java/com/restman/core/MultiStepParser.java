package com.restman.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.restman.core.TestCaseResult.Status;

public class MultiStepParser {
	private static String FINAL_STATUS = "status";
	private static String STEPS = "steps";

	public MultiStepParser() {

	}

	public String parseInput(String inputJson,Map<String,String> envContext) {
		try {
			JsonObject json = new JsonObject();
			
			Map<String, TestCase> testCaseResults = new LinkedHashMap<String, TestCase>();
			ArrayList<TestCaseResult> output = new ArrayList<TestCaseResult>();
			JsonElement jsonElement = new JsonParser().parse(inputJson);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			json.addProperty("name",jsonObject.get("name").getAsString());
			json.add("tags",jsonObject.get("tags"));
			json.addProperty("id",jsonObject.get("id").getAsString());
			json.addProperty("description",jsonObject.get("description").getAsString());
			
			Map<String, JsonObject> stepMap = new HashMap<>();
			JsonArray steps = jsonObject.getAsJsonArray("steps");
			
			JsonObject context=new JsonObject();
			
			
			for (int i = 0; i < steps.size(); i++) {
				JsonObject jo = steps.get(i).getAsJsonObject();
				stepMap.put("stepIndex" + jo.get("stepIndex").getAsInt(), jo);
				JsonObject stepContext=new JsonObject();
				stepContext.add("request", jo);
				context.add("step"+jo.get("stepIndex").getAsInt(), stepContext);
			}
			JsonObject nextStep = stepMap.get("stepIndex1");
			boolean finalStatus = true;
			
			while (nextStep != null) {
				TestCase testCase = TestCaseExecutor.getInstance().executeData(nextStep.toString(), envContext);
				testCaseResults.put(testCase.getTestCaseResult().getTestCaseId(), testCase);
				if (testCase.getTestCaseResult().getStatus() == Status.SUCCESS) {
					String nextStepId = testCase.getOnSuccess().toString();
					testCase.getTestCaseResult().setNextStep(nextStepId);
					nextStep = stepMap.get(nextStepId);
					finalStatus = (finalStatus && true);
					context.get("step"+testCase.getIndex()).getAsJsonObject().add("result",new Gson().toJsonTree(testCase.getTestCaseResult()));
				} else if (testCase.getTestCaseResult().getStatus() == Status.FAILURE) {
					String nextStepId = testCase.getOnFailure().toString();
					testCase.getTestCaseResult().setNextStep(nextStepId);
					nextStep = stepMap.get(nextStepId);
					finalStatus = (finalStatus && false);
				}
			}
			for (String key : testCaseResults.keySet()) {
				output.add(testCaseResults.get(key).getTestCaseResult());
			}
			if (finalStatus) {
				json.addProperty(FINAL_STATUS, Status.SUCCESS.toString());
			} else {
				json.addProperty(FINAL_STATUS, Status.FAILURE.toString());
			}
			json.add(STEPS, new Gson().toJsonTree(output));

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}