package com.restman.core;

import java.util.List;

@SuppressWarnings("unused")
public class TestCaseResult
{
	private String testCaseId;
	private String description;
	private String name;
	
	private Status status = Status.PENDING;
	private String nextStep;
	private List<TestAssertion> assertions;
	private List<String> tags;
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	private Object response=null;
	private String errorMessage=null;

	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public Object getResponse() {
		return response;
	}
	public void setResponse(Object response) {
		this.response = response;
	}
	public String getNextStep()
	{
		return nextStep;
	}
	public void setNextStep(String nextStep)
	{
		this.nextStep = nextStep;
	}
	public String getTestCaseId()
	{
		return testCaseId;
	}
	public void setTestCaseId(String testCaseId)
	{
		this.testCaseId = testCaseId;
	}
	public List<TestAssertion> getAssertions()
	{
		return assertions;
	}
	public void setAssertions(List<TestAssertion> assertions)
	{
		this.assertions = assertions;
	}
	public Status getStatus()
	{
		return status;
	}
	public void setStatus(Status status)
	{
		this.status = status;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public static class TestAssertion
	{
		private String name;
		private String message;
		private String expect;
		private String errorMessage;
		private Status status=Status.PENDING;

		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
		public String getMessage()
		{
			return message;
		}
		public void setMessage(String message)
		{
			this.message = message;
		}
		public String getExpect()
		{
			return expect;
		}
		public void setExpect(String expect)
		{
			this.expect = expect;
		}
		public Status getStatus()
		{
			return status;
		}
		public void setStatus(Status status)
		{
			this.status = status;
		}
		public String getErrorMessage()
		{
			return errorMessage;
		}
		public void setErrorMessage(String errorMessage)
		{
			this.errorMessage = errorMessage;
		}
	}
	public static enum Status
	{
		SUCCESS("success"),
		FAILURE("failure"),
		SKIPPED("skipped"),
		PENDING("pending");

		private String value;

		Status(String value)
		{
			this.value = value;
		}
	}
}