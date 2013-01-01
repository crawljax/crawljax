package com.crawljax.core.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.crawljax.util.Helper;

/**
 * This class represents an element. It is built from the node name and node text contents.
 * 
 * @author mesbah
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class Element implements Serializable {

	private static final long serialVersionUID = -1608999189549530008L;
	private static final int HASHCONST = 32;

	private Node node;
	private long id;
	private String tag;
	private String text;
	private List<Attribute> attributes = new ArrayList<Attribute>();

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public Element() {

	}

	/**
	 * Create a new Element.
	 * 
	 * @param node
	 *            the node used to retrieve the name and the text content from
	 */
	public Element(Node node) {
		if (node != null) {
			this.node = node;
			this.tag = node.getNodeName();
			if (node.getTextContent() == null) {
				this.text = "";
			} else {
				this.text = Helper.removeNewLines(node.getTextContent()).trim();
			}
			attributes = new ArrayList<Attribute>();
			for (int i = 0; i < node.getAttributes().getLength(); i++) {
				Node attr = node.getAttributes().item(i);
				Attribute attribute = new Attribute(attr.getNodeName(), attr.getNodeValue());
				attributes.add(attribute);
			}
		}

	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		if (!this.getText().equals("")) {
			str.append("\"");
			str.append(getText());
			str.append("\" ");
		}
		str.append(getTag().toUpperCase());
		str.append(":");
		if (getAttributes() != null) {
			for (Attribute attribute : getAttributes()) {
				str.append(" ");
				str.append(attribute.toString());
			}
		}
		return str.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof Element)) {
			return false;
		}
		return toString().equals(((Element) object).toString());
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result;
		if (attributes != null) {
			result += attributes.hashCode();
		}
		result = prime * result + (int) (id ^ (id >>> HASHCONST));
		result = prime * result;
		if (node != null) {
			result += node.hashCode();
		}
		result = prime * result;
		if (tag != null) {
			result += tag.hashCode();
		}
		result = prime * result;
		if (text != null) {
			result += text.hashCode();
		}
		return result;
	}

	/**
	 * Are all the attributes the same?
	 * 
	 * @param otherElement
	 *            the other element to compare
	 * @return true if the other attributes are equal to this one.
	 */
	public boolean equalAttributes(Element otherElement) {
		return getAttributes().toString().equalsIgnoreCase(
		        otherElement.getAttributes().toString());
	}

	/**
	 * Are both Id's the same?
	 * 
	 * @param otherElement
	 *            the other element to compare
	 * @return true if id == otherElement.id
	 */
	public boolean equalId(Element otherElement) {
		if (getElementId() == null || otherElement.getElementId() == null) {
			return false;
		}
		return getElementId().equalsIgnoreCase(otherElement.getElementId());
	}

	/**
	 * Are both the text equal?
	 * 
	 * @param otherElement
	 *            the other element to compare
	 * @return true if the text of both elements is the same
	 */
	public boolean equalText(Element otherElement) {
		return getText().equalsIgnoreCase(otherElement.getText());
	}

	/**
	 * Search for the attribute "id" and return the value.
	 * 
	 * @return the id of this element or null when not found
	 */
	public String getElementId() {
		for (Attribute attribute : getAttributes()) {
			if (attribute.getName().equalsIgnoreCase("id")) {
				return attribute.getValue();
			}
		}
		return null;
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
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag
	 *            the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the attributes
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return The node.
	 */
	public Node getNode() {
		return node;
	}
}