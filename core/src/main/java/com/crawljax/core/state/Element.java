package com.crawljax.core.state;

import java.io.Serializable;
import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Node;

import com.crawljax.util.DomUtils;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This class represents an element. It is built from the node name and node text contents.
 */
@Immutable
public class Element implements Serializable {

	private static final long serialVersionUID = -1608999189549530008L;

	private final Node node;
	private final String tag;
	private final String text;
	private final ImmutableMap<String, String> attributes;

	/**
	 * Create a new Element.
	 * 
	 * @param node
	 *            the node used to retrieve the name and the text content from. All {@link Node}
	 *            keys are saved as lowercase.
	 */
	public Element(Node node) {
		Preconditions.checkNotNull(node);
		this.node = node;
		this.tag = node.getNodeName();
		if (node.getTextContent() == null) {
			this.text = "";
		} else {
			this.text = DomUtils.removeNewLines(node.getTextContent()).trim();
		}
		Builder<String, String> builder = ImmutableMap.builder();
		for (int i = 0; i < node.getAttributes().getLength(); i++) {
			Node attr = node.getAttributes().item(i);
			builder.put(attr.getNodeName().toLowerCase(), attr.getNodeValue());
		}
		attributes = builder.build();
	}

	/**
	 * Are all the attributes the same?
	 * 
	 * @param otherElement
	 *            the other element to compare
	 * @return true if the other attributes are equal to this one.
	 */
	public boolean equalAttributes(Element otherElement) {
		return getAttributes().equals(otherElement.getAttributes());
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
		for (Entry<String, String> attribute : attributes.entrySet()) {
			if (attribute.getKey().equalsIgnoreCase("id")) {
				return attribute.getValue();
			}
		}
		return null;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param attribute
	 *            the attribute name.
	 * @return the attribute by its name or <code>null</code> if the attribute cannot be found.
	 */
	public String getAttributeOrNull(String attribute) {
		return attributes.get(attribute.toLowerCase());
	}

	/**
	 * @return The node.
	 */
	public Node getNode() {
		return node;
	}

	public ImmutableMap<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("node", node)
		        .add("tag", tag)
		        .add("text", text)
		        .add("attributes", attributes)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(node.toString(), tag, text, attributes);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Element) {
			Element that = (Element) object;
			return Objects.equal(this.node.toString(), that.node.toString())
			        && Objects.equal(this.tag, that.tag)
			        && Objects.equal(this.text, that.text)
			        && Objects.equal(this.attributes, that.attributes);
		}
		return false;
	}

}