package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Element;
import com.crawljax.core.state.Eventable;
import com.crawljax.plugins.crawloverview.CrawlOverviewException;
import com.google.common.base.Objects;

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
	private final String element;
	private final String eventType;

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
		Element el = eventable.getElement();
		if (el == null) {
			element = "unkown";
		} else {
			element = eventable.getElement().toString();
		}
		eventType = eventable.getEventType().toString();
	}

	/**
	 * @return The pre-computed hashcode.
	 */
	private final int buildHash() {
		return Objects.hashCode(from, to, hash, text, id, element, eventType);
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
	public String toString() {
		return "Edge [from=" + from + ", to=" + to + ", text=" + text + "]";
	}

	public String getId() {
		return id;
	}

	public String getEventType() {
		return eventType;
	}

	public String getElement() {
		return element;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Edge) {
			Edge that = (Edge) object;
			return Objects.equal(this.from, that.from)
			        && Objects.equal(this.to, that.to)
			        && Objects.equal(this.hash, that.hash)
			        && Objects.equal(this.text, that.text)
			        && Objects.equal(this.id, that.id)
			        && Objects.equal(this.element, that.element)
			        && Objects.equal(this.eventType, that.eventType);
		}
		return false;
	}

}
