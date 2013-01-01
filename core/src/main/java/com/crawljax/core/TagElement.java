/**
 * Created Jul 7, 2008
 */
package com.crawljax.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Class used to desribe html elements. Used internally to describe which elements to click for
 * example.
 * 
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
		StringBuffer ret = new StringBuffer();
		if (name != null) {
			ret.append(name.toUpperCase());
		}
		int i = 0;
		if (attributes != null) {
			for (TagAttribute att : attributes) {
				if (i == 0) {
					ret.append(": ");
				}
				ret.append(att.getName());
				ret.append("=\"");
				ret.append(att.getValue());
				ret.append("\" ");
				i++;
			}
		}
		return ret.toString();
	}
}
