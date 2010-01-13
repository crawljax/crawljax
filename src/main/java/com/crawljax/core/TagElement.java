/**
 * Created Jul 7, 2008
 */
package com.crawljax.core;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mesbah
 * @version $Id$
 */
public class TagElement {
	private Set<TagAttribute> attributes = new HashSet<TagAttribute>();
	private String name;
	private String id;

	/**
	 * @param attributes
	 *            the attribute set.
	 * @param name
	 *            the tag name.
	 */
	public TagElement(Set<TagAttribute> attributes, String name) {
		this.attributes = attributes;
		this.name = name;
	}

	/**
	 * The empty constructor.
	 */
	public TagElement() {
		super();
	}

	/**
	 * @return the attribute set.
	 */
	public Set<TagAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attribute set.
	 */
	public void setAttributes(Set<TagAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the tag name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the tag name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		String ret = "";
		if (name != null) {
			ret += name.toUpperCase();
		}
		int i = 0;
		for (TagAttribute att : attributes) {
			if (i == 0) {
				ret += ": ";
			}
			ret += att.getName() + "=\"" + att.getValue() + "\" ";
			i++;
		}
		return ret;
	}
}
