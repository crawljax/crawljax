package com.crawljax.plugins.testcasegenerator.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.google.gson.Gson;

public class ReportBuilder {
	private String outputPath;
	private List<MethodResult> methodRuns;
	private MethodResult currMethod;

	public ReportBuilder(String outputPath) {
		this.outputPath = outputPath;
		this.methodRuns = new LinkedList<MethodResult>();
		this.currMethod = null;
	}

	public MethodResult newMethod(String methodName) {
		if (currMethod != null) {
			methodFail();
		}
		MethodResult newMethod = new MethodResult(methodName);
		currMethod = newMethod;
		return newMethod;
	}

	public void methodSuccess() {
		if (currMethod == null)
			return;
		currMethod.setSuccess(true);
		methodRuns.add(currMethod);
		this.currMethod = null;
	}

	public void methodFail() {
		if (currMethod == null)
			return;
		currMethod.setSuccess(false);
		methodRuns.add(currMethod);
		this.currMethod = null;
	}

	public void addEventable(Eventable eventable) {
		currMethod.addEventable(eventable);
	}

	public void addState(StateVertex state) {
		currMethod.addState(state);
	}

	public void markLastEventableFailed() {
		currMethod.markLastEventableFailed();
		methodFail();
	}

	public void markLastStateFailed(List<Invariant> failedInvariants) {
		currMethod.markLastStateFailed(failedInvariants);
		methodFail();
	}

	public void markLastStateDifferent() {
		currMethod.markLastStateDifferent();
	}

	public void build() throws Exception {
		generateJson();
		copyResources();
	}

	public void generateJson() throws IOException {
		FileOutputStream file = new FileOutputStream(outputPath + "report.json");
		file.write((new Gson().toJson(methodRuns).getBytes()));
		file.close();
	}

	public void copyResources() throws IOException, URISyntaxException {
		URL skeleton = ReportBuilder.class.getResource("/webapp");
		FileUtils.copyDirectory(new File(skeleton.toURI()), new File(outputPath + "../"));
		skeleton = ReportBuilder.class.getResource("/daisydiff");
		FileUtils.copyDirectory(new File(skeleton.toURI()), new File(outputPath));
	}

	public List<MethodResult> getMethodRuns() {
		return this.methodRuns;
	}
}
