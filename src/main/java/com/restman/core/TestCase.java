package com.restman.core;

public class TestCase
{
	private TestCaseResult testCaseResult;
	private String onSuccess;
	private String onFailure;
	private int order;
	private Integer index;

	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public int getOrder()
	{
		return order;
	}
	public void setOrder(int order)
	{
		this.order = order;
	}
	public TestCase(TestCaseResult testCaseResult, String onSuccess, String onFailure)
	{
		this.testCaseResult = testCaseResult;
		this.onFailure = onFailure;
		this.onSuccess = onSuccess;
	}
	public TestCaseResult getTestCaseResult()
	{
		return testCaseResult;
	}
	public void setTestCaseResult(TestCaseResult testCaseResult)
	{
		this.testCaseResult = testCaseResult;
	}
	public String getOnSuccess()
	{
		return onSuccess;
	}
	public void setOnSuccess(String onSuccess)
	{
		this.onSuccess = onSuccess;
	}
	public String getOnFailure()
	{
		return onFailure;
	}
	public void setOnFailure(String onFailure)
	{
		this.onFailure = onFailure;
	}
}