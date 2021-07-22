/*
 * Created Dec 18, 2007
 */
package com.crawljax.core.state;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Node;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawljaxException;
import com.crawljax.forms.FormInput;
import com.crawljax.util.XPathHelper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Eventable class: an element having an event attached to it (onclick, onmouseover, ...) so that it
 * can change the DOM state.
 *
 * @author mesbah
 */
public class Eventable extends DefaultEdge implements Serializable {
	private static final long serialVersionUID = 3229708706467350994L;
	private long id;
	private EventType eventType;
	private Identification identification;
	private Element element;
	private ImmutableList<FormInput> relatedFormInputs = ImmutableList.copyOf(new ArrayList<FormInput>());
	private String relatedFrame = "";

	/**
	 * The event type.
	 */
	public enum EventType {
		click, hover, enter
	}

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public Eventable() {

	}

	/**
	 * Create a new Eventable for a identification and eventType.
	 *
	 * @param identification the identification object.
	 * @param eventType      the event type.
	 */
	public Eventable(Identification identification, EventType eventType) {
		this.identification = identification;
		this.eventType = eventType;
	}

	/**
	 * Create a new Eventable for a identification and eventType.
	 *
	 * @param identification the identification object.
	 * @param eventType      the event.
	 * @param relatedFrame   the frame containing this element.
	 */
	public Eventable(Identification identification, EventType eventType, String relatedFrame) {
		this(identification, eventType);
		this.relatedFrame = relatedFrame;
	}

	/**
	 * Create a new Eventable for a node and eventType.
	 *
	 * @param node      The DOM element.
	 * @param eventType the event type.
	 */
	public Eventable(Node node, EventType eventType) {
		this(new Identification(Identification.How.xpath, XPathHelper.getXPathExpression(node)),
				eventType);
		this.element = new Element(node);
	}

	/**
	 * Create a new Eventable for a candidateElement and eventType.
	 *
	 * @param candidateElement The CandidateElement element.
	 * @param eventType        the event type. TODO ali remove
	 */
	public Eventable(CandidateElement candidateElement, EventType eventType, long id) {
		// Constructor to create new crawl action. Giving an ID
		this(candidateElement.getIdentification(), eventType);
		if (candidateElement.getElement() != null) {
			this.element = new Element(candidateElement.getElement());
		}
		this.id=id;
		this.relatedFormInputs = ImmutableList.copyOf(candidateElement.getFormInputs());
		this.relatedFrame = candidateElement.getRelatedFrame();
	}

	/**
	 * @return the eventType.
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the identification
	 */
	public Identification getIdentification() {
		return identification;
	}

	/**
	 * @param identification the identification to set
	 */
	public void setIdentification(Identification identification) {
		this.identification = identification;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * @param element the element to set
	 */
	public void setElement(Element element) {
		this.element = element;
	}

	/**
	 * Retrieve the related form inputs.
	 *
	 * @return the formInputs
	 */
	public ImmutableList<FormInput> getRelatedFormInputs() {
		return relatedFormInputs;
	}

	/**
	 * Set the list of formInputs.
	 *
	 * @param relatedFormInputs the list of formInputs
	 */
	public void setRelatedFormInputs(List<FormInput> relatedFormInputs) {
		this.relatedFormInputs = ImmutableList.copyOf(relatedFormInputs);;
	}

	/**
	 * @return the source state.
	 * @throws CrawljaxException if the source cannot be found.
	 */
	public StateVertex getSourceStateVertex() {
		return (StateVertex) getSource();

	}

	/**
	 * @return the target state.
	 * @throws CrawljaxException if the target cannot be found.
	 */
	public StateVertex getTargetStateVertex() {
		return (StateVertex) getTarget();
	}

	/**
	 * @return the relatedFrame
	 */
	public String getRelatedFrame() {
		return relatedFrame;
	}

	@VisibleForTesting
	public void setSource(StateVertex source) {
		setField("source", source);
	}

	private void setField(String fieldName, StateVertex source) {
		Class<?> superclass = Eventable.class.getSuperclass().getSuperclass();
		try {
			Field f = superclass.getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(this, source);
		} catch (ReflectiveOperationException e) {
			throw new CrawljaxException("Could not set the source field", e);
		}
	}

	@VisibleForTesting
	public void setTarget(StateVertex target) {
		setField("target", target);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("eventType", eventType)
				.add("identification", identification)
				.add("element", element)
				.add("source", getSource())
				.add("target", getTarget())
				.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(eventType, identification, element,
				this.getSource(), this.getTarget());
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Eventable) {
			Eventable that = (Eventable) object;
			return Objects.equal(this.eventType, that.eventType)
					&& Objects.equal(this.identification, that.identification)
					&& Objects.equal(this.element, that.element)
					&& Objects.equal(this.getSource(), that.getSource())
					&& Objects.equal(this.getTarget(), that.getTarget());
		}
		return false;
	}
	
	public Object getEdgeSource() {
		return getSource();
	}
	
	public Object getEdgeTarget() {
		return getTarget();
	}
}
