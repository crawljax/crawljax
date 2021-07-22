package com.crawljax.plugins.testcasegenerator.report;

import java.util.LinkedList;
import java.util.List;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.testcasegenerator.report.MethodResult.WarnLevel;

public class ReportBuilder {
	private List<MethodResult> methodRuns;
	private MethodResult currMethod;

	public ReportBuilder() {
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
	
	public StateVertexResult getLastState() {
		return currMethod.getLastState();
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
	
	public void setLastStateComparison(String compResult) {
		currMethod.setLastStateComparison(compResult);
	}
	
	
	public void setWarnLevel(WarnLevel level) {
		currMethod.setWarnLevel(level);
	}
	
	public void setLocatorWarning(boolean broken) {
		currMethod.setLocatorWarning(broken);
	}

//	public void build() throws Exception {
//		generateJson();
//		copyResources();
//	}

//	public void generateJson() throws IOException {
//		FileOutputStream file = new FileOutputStream(outputPath + "report.json");
//		file.write((new Gson().toJson(methodRuns).getBytes()));
//		file.close();
//	}

//	public void copyResources() throws IOException, URISyntaxException {
//		URL skeleton = ReportBuilder.class.getResource("/webapp");
//		FileUtils.copyDirectory(new File(skeleton.toURI()), new File(outputPath + "../"));
//		skeleton = ReportBuilder.class.getResource("/daisydiff");
//		FileUtils.copyDirectory(new File(skeleton.toURI()), new File(outputPath));
//	}

	public List<MethodResult> getMethodRuns() {
		return this.methodRuns;
	}
}
