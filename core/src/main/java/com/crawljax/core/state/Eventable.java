/**
 * Created Dec 18, 2007
 */
package com.crawljax.core.state;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Node;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawljaxException;
import com.crawljax.forms.FormInput;
import com.crawljax.util.XPathHelper;

/**
 * Eventable class: an element having an event attached to it (onclick, onmouseover, ...) so that it
 * can change the DOM state.
 * 
 * @author mesbah
 * @version $Id$
 */
public class Eventable extends DefaultEdge implements Serializable {
	private static final long serialVersionUID = 3229708706467350994L;
	private long id;
	private EventType eventType;
	private Identification identification;
	private Element element;
	private List<FormInput> relatedFormInputs = new ArrayList<FormInput>();
	private String relatedFrame = "";

	/**
	 * The event type.
	 */
	public enum EventType {
		click, hover
	}

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public Eventable() {

	}

	/**
	 * Create a new Eventable for a identification and eventType.
	 * 
	 * @param identification
	 *            the identification object.
	 * @param eventType
	 *            the event type.
	 */
	public Eventable(Identification identification, EventType eventType) {
		this.identification = identification;
		this.eventType = eventType;
	}

	/**
	 * Create a new Eventable for a identification and eventType.
	 * 
	 * @param identification
	 *            the identification object.
	 * @param eventType
	 *            the event.
	 * @param relatedFrame
	 *            the frame containing this element.
	 */
	public Eventable(Identification identification, EventType eventType, String relatedFrame) {
		this(identification, eventType);
		this.relatedFrame = relatedFrame;
	}

	/**
	 * Create a new Eventable for a node and eventType.
	 * 
	 * @param node
	 *            The DOM element.
	 * @param eventType
	 *            the event type.
	 */
	public Eventable(Node node, EventType eventType) {
		this(new Identification(Identification.How.xpath, XPathHelper.getXPathExpression(node)),
		        eventType);
		this.element = new Element(node);
	}

	/**
	 * Create a new Eventable for a candidateElement and eventType.
	 * 
	 * @param candidateElement
	 *            The CandidateElement element.
	 * @param eventType
	 *            the event type. TODO ali remove
	 */
	public Eventable(CandidateElement candidateElement, EventType eventType) {
		this(candidateElement.getIdentification(), eventType);
		if (candidateElement.getElement() != null) {
			this.element = new Element(candidateElement.getElement());
		}
		this.relatedFormInputs = candidateElement.getFormInputs();
		this.relatedFrame = candidateElement.getRelatedFrame();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return the String representation of this Eventable
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.element != null) {
			sb.append(this.getElement());
		}
		sb.append(' ').append(this.eventType).append(' ').append(this.identification);
		return sb.toString();
	}

	/**
	 * Returns a hashcode. Uses reflection to determine the fields to test.
	 * 
	 * @return the hashCode
	 */
	@Override
	public int hashCode() {
		String[] exclude = new String[1];
		exclude[0] = "id";

		return HashCodeBuilder.reflectionHashCode(this, exclude);
	}

	/**
	 * Return true if equal. Uses reflection.
	 * 
	 * @param obj
	 *            the object to compare to of type Eventable
	 * @return true if both Objects are equal
	 */

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Eventable)) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		Eventable rhs = (Eventable) obj;

		return new EqualsBuilder().append(toString(), rhs.toString()).isEquals();
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
	 * @param id
	 *            the id to set
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
	 * @param identification
	 *            the identification to set
	 */
	public void setIdentification(Identification identification) {
		this.identification = identification;
	}

	/**
	 * @param eventType
	 *            the eventType to set
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
	 * @param element
	 *            the element to set
	 */
	public void setElement(Element element) {
		this.element = element;
	}

	/**
	 * Retrieve the related form inputs.
	 * 
	 * @return the formInputs
	 */
	public List<FormInput> getRelatedFormInputs() {
		return relatedFormInputs;
	}

	/**
	 * Set the list of formInputs.
	 * 
	 * @param relatedFormInputs
	 *            the list of formInputs
	 */
	public void setRelatedFormInputs(List<FormInput> relatedFormInputs) {
		this.relatedFormInputs = relatedFormInputs;
	}

	/* Horrible stuff happening below, don't look at it! */

	/**
	 * @return the source state.
	 * @throws CrawljaxException
	 *             if the source cannot be found.
	 */
	public StateVertex getSourceStateVertex() throws CrawljaxException {
		return getSuperField("source");
	}

	/**
	 * @return the target state.
	 * @throws CrawljaxException
	 *             if the target cannot be found.
	 */
	public StateVertex getTargetStateVertex() throws CrawljaxException {
		return getSuperField("target");
	}

	private StateVertex getSuperField(String name) throws CrawljaxException {
		try {
			return (StateVertex) searchSuperField(name).get(this);
		} catch (IllegalArgumentException e) {
			throw new CrawljaxException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}

	private Field searchSuperField(String name) {
		Class<?> clazz = this.getClass().getSuperclass().getSuperclass();
		Field[] fields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);

		for (Field field : fields) {
			String fieldName = field.getName();

			if (name.equals(fieldName)) {
				return field;
			}
		}
		throw new InternalError("Field was not found!");
	}

	/**
	 * @return the relatedFrame
	 */
	public String getRelatedFrame() {
		return relatedFrame;
	}
}
