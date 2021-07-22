package com.crawljax.plugins.testcasegenerator.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.crawljax.plugins.testcasegenerator.TestSuiteHelper;
import com.crawljax.plugins.testcasegenerator.fragDiff.FragDiff;

public class TestRecord {
	private int methodNumber;
	private String methodName;
	private Date createTime;
	private Date startTime;
	private long duration;
	private String testSrcPath;
	private String outputFolder;
	private TestStatusType testStatus = TestStatusType.idle;
	private List<TestStateDiff> diffs = new ArrayList<TestStateDiff>();
	private String failureMessage = "none";
	private MethodResult methodResult;

	public enum TestStatusType {
		idle, queued, initializing, running, success, failure, skipped
	}

	/**
	 * @return the id
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName
	 *            the name of the method
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime
	 *            the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * return the path to the test's generated source code
	 */
	public String getTestSrcPath() {
		return testSrcPath;
	}

	/**
	 * @param testSrcPath
	 *            the path to the test's generated source code
	 */
	public void setTestSrcPath(String testSrcPath) {
		this.testSrcPath = testSrcPath;
	}

	/**
	 * @return the outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/**
	 * @param outputFolder
	 *            the outputFolder to set
	 */
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * @return the crawlStatus
	 */
	public TestStatusType getTestStatus() {
		return testStatus;
	}

	public MethodResult getMethodResult() {
		return methodResult;
	}

	/**
	 * @param testStatus
	 *            the test status to set
	 */
	public void setTestStatus(TestStatusType testStatus) {
		this.testStatus = testStatus;
	}

	public List<TestStateDiff> getDiffs() {
		return diffs;
	}

	public void setDiffs(List<TestStateDiff> diffs) {
		this.diffs = diffs;
	}

	public void addDiff(TestStateDiff diff) {
		this.diffs.add(diff);
	}

	public void setFailureMessage(String message) {
		this.failureMessage = message;
	}

	public void setMethodResult(MethodResult methodResult) {
		this.methodResult = methodResult;
	}

	public void setTestRecordStatus(TestStatusType status, String message) {
		setTestStatus(status);
		setFailureMessage(message);
	}

	public int getMethodNumber() {
		return methodNumber;
	}

	public void setMethodNumber(int methodNumber) {
		this.methodNumber = methodNumber;
	}

	public String getFailureMessage() {
		return failureMessage;
	}
	
	/**
	 * @param vertexName
	 *            The name of the state vertex to which this diff corresponds to
	 * @param oldState
	 *            old state
	 * @param newState
	 *            new state
	 * @param compResult TODO
	 */
	public void writeDiffToTestRecord(String vertexName, String oldState, String newState, String compResult) {
		TestStateDiff diff = new TestStateDiff();
		diff.setState(vertexName);
		diff.setOldState(oldState);
		diff.setNewState(newState);
		diff.setCompResult(compResult);
		addDiff(diff);
	}

	public void writeDiffToTestRecord(String vertexName, FragDiff fragDiff) {
		TestStateDiff diff = new TestStateDiff();
		diff.setState(vertexName);
		diff.setOldState(fragDiff.getOldFile());
		diff.setNewState(fragDiff.getNewFile());
		diff.setCompResult(fragDiff.getComp());
		addDiff(diff);
	}
}