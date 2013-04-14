package com.crawljax.core;

import com.crawljax.core.state.Eventable;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Objects;

/**
 * Represents a task that has to be run by a {@link CrawlTaskConsumer}.
 */
public class CrawlTask {

	private final ImmutableList<Eventable> eventables;

	CrawlTask(ImmutableList<Eventable> eventables) {
		this.eventables = eventables;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(eventables);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CrawlTask) {
			CrawlTask that = (CrawlTask) object;
			return Objects.equal(this.eventables, that.eventables);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("eventables", eventables)
		        .toString();
	}

}
