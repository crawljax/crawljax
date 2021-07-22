package com.crawljax.plugins.testcasegenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlPathInfo;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Element;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormInput;
import com.crawljax.util.FSUtils;
import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class TestSuiteGeneratorHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteGeneratorHelper.class
	        .getName());

	private final CrawlSession session;

	public TestSuiteGeneratorHelper(CrawlSession session) {
		this.session = session;
//		updateIdsForEventables();
	}

	private void updateIdsForEventables() {
		long id = 1;
		for (Eventable eventable : session.getStateFlowGraph().getAllEdges()) {
			eventable.setId(id);
			id++;
		}
	}

	public void writeStateVertexTestDataToJSON(String fname) throws IOException {
		Set<StateVertex> states = session.getStateFlowGraph().getAllStates();

		FSUtils.checkFolderForFile(fname);
		LOGGER.info("Writing StateVertices test data to " + fname);
		Map<Long, StateVertex> map = new HashMap<Long, StateVertex>();
		for (StateVertex stateVertex : states) {
			// stateVertex.setId(id);
			// Original StateVertex saveSateVertix = stateVertex.clone();
			StateVertex saveStateVertex = stateVertex;
			map.put((long) saveStateVertex.getId(), saveStateVertex);
		}

		FileOutputStream fo = new FileOutputStream(fname);
		fo.write((new Gson()).toJson(map).getBytes());
		fo.close();
	}
	
	public static void writeEventableTestDataToJson(String fname, Collection<Eventable> eventables) throws IOException  {
		FSUtils.checkFolderForFile(fname);
		LOGGER.info("Writing Eventables test data to " + fname);
		Map<Long, Eventable> map = new HashMap<Long, Eventable>();
		long id = 1;
		for (Eventable eventable : eventables) {
			Eventable newEventable =
			        new Eventable(eventable.getIdentification(), eventable.getEventType(),
			                eventable.getRelatedFrame());
			newEventable.setId(eventable.getId());
			Element element = eventable.getElement();
			newEventable.setElement(element);

			map.put(newEventable.getId(), newEventable);
			id++;
		}

		FileOutputStream fo = new FileOutputStream(fname);
		fo.write((new Gson()).toJson(map).getBytes());
		fo.close();
	}
	
	public void writeEventableTestDataToJSON(String fname) throws IOException {
		Set<Eventable> eventables = session.getStateFlowGraph().getAllEdges();
		List<Eventable> expired = ((InMemoryStateFlowGraph)session.getStateFlowGraph()).getExpiredEdges();
		expired.addAll(eventables);
		writeEventableTestDataToJson(fname, expired);
	}
	
	

	public List<TestMethod> getTestMethods() {
		Collection<List<Eventable>> crawlPaths = session.getCrawlPaths();
		return getTestMethods(crawlPaths);
//		, session.getPathInfoMap());
	}
	
	public static String getEventableInfo(Eventable eventable) {
		if((eventable.getEdgeSource() instanceof LinkedTreeMap) || (eventable.getEdgeTarget() instanceof LinkedTreeMap)){
			return MoreObjects.toStringHelper(eventable)
					.add("eventType", eventable.getEventType())
					.add("identification", eventable.getIdentification())
					.add("element", eventable.getElement())
					.add("source", "" + (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeSource()).get("id").toString()))
					.add("target", "" + (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeTarget()).get("id").toString()))
					.toString();
		}
		return eventable.toString();
	}

	public static List<TestMethod> getTestMethods(Collection<List<Eventable>> crawlPaths) {
		List<String> methodNames = new ArrayList<String>();
 		List<TestMethod> testMethods = new ArrayList<TestMethod>();

		TestMethod testMethod;
		TestMethodEvent methodEvent;
		Map<String, String> properties;

		// initial state testing
		properties = new HashMap<String, String>();
		properties.put("index", "0");
		properties.put("targetid", "0");
		properties.put("how", "index");
		methodEvent = new TestMethodEvent();
		methodEvent.setProperties(properties);

		testMethod = new TestMethod();
		testMethod.setMethodName("0");
		testMethod.addMethodEvent(methodEvent);
		testMethods.add(testMethod);

		// add the paths
		int iter = 0;
		for (List<Eventable> crawlPath : crawlPaths) {
			if (crawlPath.size() == 0)
				continue;
			iter+=1;
			testMethod = new TestMethod();
			String methodId = "";
//			if(pathInfoMap!=null && pathInfoMap.containsKey(crawlPath)) {
			if(crawlPath instanceof CrawlPath) {
				CrawlPath pathCast = (CrawlPath)crawlPath;
				CrawlPathInfo info = new CrawlPathInfo(iter, pathCast.getBacktrackTarget(), pathCast.isBacktrackSuccess(), pathCast.isReachedNearDup());
				methodId += "BT" + info.getBacktrackTarget() + "";
				methodId += info.isBacktrackSuccess() ? "":"_failed";
				methodId += info.isReachedNearDup()==-1?"":"_"+info.isReachedNearDup();
				methodId += "_path";
			}

			for (Eventable clickable : crawlPath) {
				// set properties of methodEvent
				properties = new HashMap<String, String>();
				properties.put("id", "" + clickable.getId());
				properties.put("info", getEventableInfo(clickable));
				properties.put("how", clickable.getIdentification().getHow().toString());
				if(clickable.getElement()!=null)
					properties.put("text", clickable.getElement().getText()
				        .replaceAll("\"", "\\\\\"").trim());
				else
					properties.put("text", "");
				try {
					String sourceid = null;
					if(clickable.getEdgeSource() instanceof StateVertex)	
						sourceid = "" + clickable.getSourceStateVertex().getId();
					if(clickable.getEdgeSource() instanceof LinkedTreeMap) {
						sourceid = "" + (int)Double.parseDouble(((LinkedTreeMap<?, ?>)clickable.getEdgeSource()).get("id").toString());
					}
					properties.put("sourceid", sourceid);
				} catch (CrawljaxException e) {
					LOGGER.error("Catched CrawljaxException while getting SourceStateVertex", e);
				}
				try {
					String targetid = null;
					if(clickable.getEdgeTarget() instanceof StateVertex)	
						targetid = "" + clickable.getTargetStateVertex().getId();
					if(clickable.getEdgeTarget() instanceof LinkedTreeMap) {
						targetid = "" + (int)Double.parseDouble(((LinkedTreeMap<?, ?>)clickable.getEdgeTarget()).get("id").toString());
					}
					properties.put("targetid", targetid);
				} catch (CrawljaxException e) {
					LOGGER.error("Catched CrawljaxException while getting TargetStateVertex", e);
				}
				String id = "" + clickable.getId();
				properties.put("index", id);
				methodEvent = new TestMethodEvent();
				methodEvent.setProperties(properties);

				// set formInputs
				List<Map<String, String>> mapFormInputs = new ArrayList<Map<String, String>>();
				for (FormInput formInput : clickable.getRelatedFormInputs()) {
					if (formInput.getInputValues().iterator().hasNext()) {
						properties = new HashMap<String, String>();
						// TODO Changed to make compile. Is this correct?
						properties.put("how", formInput.getIdentification().getHow().toString());

						properties.put("name", formInput.getIdentification().getValue());
						properties.put("type", formInput.getType().toString());
						properties.put("value", formInput.getInputValues().iterator().next()
						        .getValue());
						mapFormInputs.add(properties);
					}
				}
				methodEvent.setFormInputs(mapFormInputs);

				// append to method name
				if (!methodId.equals("")) {
					methodId += "_";
				}
				methodId += id;

				// add event
				testMethod.addMethodEvent(methodEvent);
			}

			// set the name and add the test method
			if(methodNames.contains(methodId)) {
				methodId = methodId + "_dup";
				int i = 0;
				while(true) {
					
					if (methodNames.contains(methodId+i)) {
						i = i +1;
					}
					else {
						methodId = methodId += i;
						break;
					}
				}
			}
			testMethod.setMethodName(methodId);
			testMethods.add(testMethod);
			methodNames.add(methodId);
		}

		return testMethods;
	}
}
