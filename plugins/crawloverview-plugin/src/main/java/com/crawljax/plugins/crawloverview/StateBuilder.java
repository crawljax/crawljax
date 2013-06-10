package com.crawljax.plugins.crawloverview;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.State;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

class StateBuilder {

	private final BlockingQueue<CandidateElementPosition> candidates = Queues
	        .newLinkedBlockingQueue();
	private final StateVertex state;
	private final AtomicInteger fanIn = new AtomicInteger();
	private final AtomicInteger fanOut = new AtomicInteger();
	private final ImmutableList.Builder<String> failedEvents = new ImmutableList.Builder<>();

	public StateBuilder(StateVertex state) {
		this.state = state;
	}

	public String getName() {
		return state.getName();
	}

	public boolean addCandidates(Collection<CandidateElementPosition> candidates) {
		return this.candidates.addAll(candidates);
	}

	public ImmutableList<CandidateElementPosition> getCandidates() {
		List<CandidateElementPosition> buffer = Lists
		        .newArrayListWithCapacity(candidates.size());
		candidates.drainTo(buffer);
		return ImmutableList.copyOf(buffer);
	}

	public int incrementFanOut() {
		return fanOut.incrementAndGet();
	}

	public int incrementFanIn() {
		return fanIn.incrementAndGet();
	}

	public State build() {
		return new State(state, fanIn.get(), fanOut.get(), getCandidates(),
		        failedEvents.build());
	}

	public void eventFailed(Eventable eventable) {
		failedEvents.add(eventable.getIdentification().toString());
	}

}
