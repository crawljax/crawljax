/**
 * Created Dec 18, 2007
 */
package com.crawljax.core.state;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Node;

import com.crawljax.core.CandidateElement;
import com.crawljax.forms.FormInput;
import com.crawljax.util.XPathHelper;

/**
 * Eventable class: an element having an event attached to it (onclick, onmouseover, ...) so that it
 * can change the DOM state.
 * 
 * @author mesbah
 * @version $Id: Eventable.java 6374 2009-12-29 09:31:52Z frank $
 */
public class Eventable extends DefaultEdge {
	private static final long serialVersionUID = 3229708706467350994L;
	private long id;
	private String eventType;
	private Identification identification;
	private Element element;
	private Edge edge;
	private List<FormInput> relatedFormInputs = new ArrayList<FormInput>();

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
	 *            the event.
	 */
	public Eventable(Identification identification, String eventType) {
		this.identification = identification;
		this.eventType = eventType;
	}

	/**
	 * Create a new Eventable for a node and eventType.
	 * 
	 * @param node
	 *            The DOM element.
	 * @param eventType
	 *            the event type.
	 */
	public Eventable(Node node, String eventType) {
		this(new Identification("xpath", XPathHelper.getXpathExpression(node)), eventType);
		this.element = new Element(node);
	}

	/**
	 * Create a new Eventable for a candidateElement and eventType.
	 * 
	 * @param candidateElement
	 *            The CandidateElement element.
	 * @param eventType
	 *            the event type.
	 */
	public Eventable(CandidateElement candidateElement, String eventType) {
		this(new Identification("xpath", candidateElement.getXpath()), eventType);
		this.element = new Element(candidateElement.getElement());
		this.relatedFormInputs = candidateElement.getFormInputs();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return the String representation of this Eventable
	 */
	@Override
	public String toString() {
		String str = "";
		if (this.getElement() != null) {
			str = this.getElement().toString();
		}
		str += " " + this.eventType + " " + this.identification.toString();
		return str;
	}

	/**
	 * Returns a hashcode. Uses reflection to determine the fields to test.
	 * 
	 * @return the hashCode
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
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
	public String getEventType() {
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
	 * Retrieve the edge associated with this eventalbe.
	 * 
	 * @return the edge associated
	 */
	public synchronized Edge getEdge() {
		return edge;
	}

	/**
	 * Set the Edge assosiaded witht this eventable.
	 * 
	 * @param edge
	 *            the Edge to set
	 */
	public synchronized void setEdge(Edge edge) {
		this.edge = edge;
	}

	/**
	 * @param eventType
	 *            the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return DOCUMENT ME!
	 */
	// TODO: Ali, replace with edge.getToStateVertix()?
	@SuppressWarnings("unchecked")
	public StateVertix getTarget() {
		Class clazz = this.getClass().getSuperclass().getSuperclass();
		Field[] fields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();

			try {
				// Warning: Field.get(Object) creates wrappers objects
				// for primitive types.
				if ("target".equals(fieldName)) {
					StateVertix fieldValue = (StateVertix) field.get(this);

					return fieldValue;
				}
			} catch (IllegalAccessException ex) {
				throw new InternalError("Unexpected IllegalAccessException: " + ex.getMessage());
			}
		}

		return null;
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

}
