package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * An {@link Edge} between two {@link State}s.
 */
@Immutable
public class AlchemyEdge {

	private final int source;
	private final int target;
	private final int hash;
	private final String text;
	private final String element;
	private final String eventType;

	@JsonCreator
	public AlchemyEdge(@JsonProperty("source") int source, @JsonProperty("target") int target,
	        @JsonProperty("hash") int hash, @JsonProperty("text") String text,
	        @JsonProperty("element") String element,
	        @JsonProperty("eventType") String eventType) {
		this.source = source;
		this.target = target;
		this.hash = hash;
		this.text = text;
		this.element = element;
		this.eventType = eventType;
	}

	/**
	 * @return The pre-computed hashcode.
	 */
	private final int buildHash() {
		return Objects.hashCode(source, target, text, element, eventType);
	}

	public int getSource() {
		return source;
	}

	public int getTarget() {
		return target;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "Edge [source=" + source + ", target=" + target + ", text=" + text + "]";
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
		if (object instanceof AlchemyEdge) {
			AlchemyEdge that = (AlchemyEdge) object;
			return Objects.equal(this.source, that.source)
			        && Objects.equal(this.target, that.target)
			        && Objects.equal(this.text, that.text)
			        && Objects.equal(this.element, that.element)
			        && Objects.equal(this.eventType, that.eventType);
		}
		return false;
	}

}