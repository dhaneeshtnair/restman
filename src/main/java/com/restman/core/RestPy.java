package com.restman.core;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("unused")
@Path("/rtest")
public class RestPy
{
	@POST
	@Path("{testCaseId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String test(String body)
	{
		JsonObject jsonObject = (JsonObject) new JsonParser().parse(body);
		return "";
	}

	@POST
	@Path("execute")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String execute(String body)
	{
//		TestCaseResult testCaseResult = TestCaseExecutor.getInstance().executeData(body,new HashMap<>());
//		System.out.println("Task Completed!");
//		return new Gson().toJson(testCaseResult);

		MultiStepParser multiStep = new MultiStepParser();
		return multiStep.parseInput(body,new HashMap<>());
	}
}