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
 * @version $Id$
 */
public class Eventable extends DefaultEdge implements Cloneable {
	private static final long serialVersionUID = 3229708706467350994L;
	private long id;
	private String eventType;
	private Identification identification;
	private Element element;

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
	 * @param eventType
	 *            the eventType to set
	 */
	public void setEventType(String eventType) {
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

	/**
	 * Return a new clone of this object.
	 * 
	 * @return the cloned Eventalbe
	 */
	@Override
	public Eventable clone() {
		Eventable e = new Eventable();

		ArrayList<FormInput> fi = new ArrayList<FormInput>();
		for (FormInput formInput : this.relatedFormInputs) {
			fi.add(formInput.clone());
		}
		if (this.element != null) {
			e.setElement(this.element.clone());
		}
		e.setEventType(this.eventType);
		e.setId(this.id);
		if (this.identification != null) {
			e.setIdentification(this.identification.clone());
		}
		e.setRelatedFormInputs(fi);

		e.setSourceStateVertix(this.getSourceStateVertix());
		e.setTargetStateVertix(this.getTargetStateVertix());

		return e;
	}

	/* Horrible stuff happening below, don't look at it! */

	/**
	 * @return the source state.
	 */
	public StateVertix getSourceStateVertix() {
		return getSuperField("source");
	}

	/**
	 * @return the target state.
	 */
	public StateVertix getTargetStateVertix() {
		return getSuperField("target");
	}

	/**
	 * @param vertix
	 *            the new value for source
	 */
	public void setSourceStateVertix(StateVertix vertix) {
		setSuperField("source", vertix);
	}

	/**
	 * @param vertix
	 *            the new value for target
	 */
	public void setTargetStateVertix(StateVertix vertix) {
		setSuperField("target", vertix);
	}

	private StateVertix getSuperField(String name) {
		try {
			return (StateVertix) searchSuperField(name).get(this);
		} catch (IllegalArgumentException e) {
			// TODO Log
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Log
			e.printStackTrace();
		}
		return null;
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

	private void setSuperField(String name, StateVertix vertix) {
		try {
			searchSuperField(name).set(this, vertix);
		} catch (IllegalArgumentException e) {
			// TODO Log
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Log
			e.printStackTrace();
		}
	}
}
