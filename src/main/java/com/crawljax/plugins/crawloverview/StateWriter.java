package com.crawljax.plugins.crawloverview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.State;

public class StateWriter {

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

		// writing
		String name = state.getName();
		outBuilder.writeState(context, name);
	}

	private List<Map<String, String>> getElements(StateFlowGraph sfg, State state) {
		List<Map<String, String>> elements = new ArrayList<Map<String, String>>();

		for (RenderedCandidateElement element : state.getCandidateElements()) {
			Eventable eventable = getEventableByCandidateElementInState(state, element);
			StateVertex toState = null;
			Map<String, String> elementMap = new HashMap<String, String>();
			Point offset = state.getScreenShotOffset();
			elementMap.put("left", "" + (element.getLocation().x - 3 + offset.getY()));
			elementMap.put("top", "" + (element.getLocation().y - 3 + offset.getX()));
			elementMap.put("width", "" + (element.getSize().width + 2));
			elementMap.put("height", "" + (element.getSize().height + 2));
			if (eventable != null) {
				toState = sfg.getTargetState(eventable);
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
	        RenderedCandidateElement element) {
		StateVertex vertex = visitedStates.get(state.getName());
		for (Eventable eventable : sfg.getOutgoingClickables(vertex)) {
			// TODO Check if element.getIdentification().getValue() is correct replacement for
			// element.getXpath()
			if (eventable.getIdentification().getValue()
			        .equals(element.getIdentification().getValue())) {
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
