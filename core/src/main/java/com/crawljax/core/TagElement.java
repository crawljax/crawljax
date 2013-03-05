package com.crawljax.core;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.configuration.CrawlAttribute;
import com.crawljax.core.configuration.CrawlElement;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Class used to desribe html elements. Used internally to describe which elements to click for
 * example.
 */
@Immutable
public class TagElement {
	private ImmutableSet<TagAttribute> attributes;
	private String name;
	private String id;

	/**
	 * @param attributes
	 *            the attribute set.
	 * @param name
	 *            the tag name. This will be transformed to captial letters.
	 */
	public TagElement(ImmutableSet<TagAttribute> attributes, String name, String id) {
		Preconditions.checkNotNull(attributes);
		Preconditions.checkNotNull(name);
		this.attributes = attributes;
		this.name = name.toUpperCase();
		this.id = id;
	}

	public TagElement(CrawlElement crawlElement) {
		ImmutableSet.Builder<TagAttribute> attributes = ImmutableSet.builder();
		for (CrawlAttribute crawlAttribute : crawlElement.getCrawlAttributes()) {
			attributes.add(new TagAttribute(crawlAttribute.getName(), crawlAttribute.getValue()));
		}
		this.attributes = attributes.build();
		this.name = crawlElement.getTagName();
		this.id = crawlElement.getId();
	}

	/**
	 * @return the attribute set.
	 */
	public ImmutableSet<TagAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @return the tag name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the id.
	 */
	public String getId() {
		return id;
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
