package com.crawljax.plugins.crawloverview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.State;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

class StateWriter {

	private static final Logger LOG = LoggerFactory.getLogger(StateWriter.class);

	private static final String COLOR_NEW_STATE = "green";
	private static final String COLOR_A_PREVIOUS_STATE = "#00FFFF";
	private static final String COLOR_NO_STATE_CHANGE = "orange";

	private final OutputBuilder outBuilder;
	private final StateFlowGraph sfg;
	private final Map<String, StateVertex> visitedStates;

	public StateWriter(OutputBuilder outBuilder, StateFlowGraph sfg,
	        Map<String, StateVertex> visitedStates) {
		this.outBuilder = outBuilder;
		this.sfg = sfg;
		this.visitedStates = visitedStates;
	}

	void writeHtmlForState(State state) {
		LOG.debug("Writing state file for state {}", state.getName());
		VelocityContext context = new VelocityContext();
		context.put("name", state.getName());
		context.put("screenshot", state.getName() + ".png");
		context.put("elements", getElements(sfg, state));
		context.put("fanIn", state.getFanIn());
		context.put("fanOut", state.getFanOut());
		context.put("url", state.getUrl());
		context.put("cluster", state.getCluster());

		String failedEvents = "-";
		if (!state.getFailedEvents().isEmpty()) {
			failedEvents = Joiner.on(", ").join(state.getFailedEvents());
		}
		context.put("failedEvents", failedEvents);
		String dom = outBuilder.getDom(state.getName());
		dom = StringEscapeUtils.escapeHtml4(dom);
		context.put("dom", dom);

		// For threshold
		context.put("hasNearDuplicate", state.isHasNearDuplicate());
		context.put("nearestState", state.getNearestState());
		context.put("distToNearestState", state.getDistToNearestState());

		// writing
		String name = state.getName();
		outBuilder.writeState(context, name);
	}

	private List<Map<String, String>> getElements(StateFlowGraph sfg, State state) {
		List<CandidateElementPosition> candidateElements = state.getCandidateElements();

		List<Map<String, String>> elements =
		        Lists.newArrayListWithCapacity(candidateElements.size());

		for (CandidateElementPosition element : candidateElements) {
			Eventable eventable = getEventableByCandidateElementInState(state, element);
			StateVertex toState = null;
			Map<String, String> elementMap = new HashMap<String, String>();
			elementMap.put("xpath", element.getXpath());
			elementMap
			        .put("left", "" + (element.getLeft() - 3));
			elementMap.put("top", "" + (element.getTop() - 3));
			elementMap.put("width", "" + (element.getWidth() + 2));
			elementMap.put("height", "" + (element.getHeight() + 2));
			if (eventable != null) {
				toState = eventable.getTargetStateVertex();
			}
			if (toState != null) {
				elementMap.put("targetname", toState.getName());
				if (getStateNumber(toState.getName()) < getStateNumber(state.getName())) {
					// state already found
					elementMap.put("color", COLOR_A_PREVIOUS_STATE);
				} else {
					// new state
					elementMap.put("color", COLOR_NEW_STATE);
				}
				elementMap.put("zindex", "20");
				elementMap.put("cursor", "pointer");
			} else {
				elementMap.put("targetname", "");
				// no state change ater clicking or is not clicked
				elementMap.put("color", COLOR_NO_STATE_CHANGE);
				elementMap.put("cursor", "normal");
				elementMap.put("zindex", "10");
			}
			elements.add(elementMap);
		}

		return elements;
	}

	private Eventable getEventableByCandidateElementInState(State state,
	        CandidateElementPosition element) {
		StateVertex vertex = visitedStates.get(state.getName());
		for (Eventable eventable : sfg.getOutgoingClickables(vertex)) {
			// TODO Check if element.getIdentification().getValue() is correct replacement for
			// element.getXpath()
			if (eventable.getIdentification().getValue().equals(element.getXpath())) {
				return eventable;
			}
		}
		return null;
	}

	private int getStateNumber(String name) {
		if ("index".equals(name)) {
			return 0;
		}
		return Integer.parseInt(name.replace("state", ""));
	}
}
