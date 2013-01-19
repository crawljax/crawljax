package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.plugins.crawloverview.CrawlOverviewException;

/**
 * An {@link Edge} between two {@link State}s.
 */
@Immutable
public class Edge {

	private final String from;
	private final String to;
	private final int hash;
	private final String text;
	private final String id;

	public Edge(Eventable eventable) {
		try {
			this.from = eventable.getSourceStateVertex().getName();
			this.to = eventable.getTargetStateVertex().getName();
		} catch (CrawljaxException e) {
			throw new CrawlOverviewException("Could not get state vertex", e);
		}
		this.text = eventable.getElement().getText();
		this.hash = buildHash();
		this.id = eventable.getIdentification().toString();
	}

	private final int buildHash() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		return other.hashCode() == hash;
	}

	@Override
	public String toString() {
		return "Edge [from=" + from + ", to=" + to + ", text=" + text + "]";
	}

	public String getId() {
		return id;
	}

}
