package com.crawljax.core.configuration;

import com.crawljax.core.state.Eventable.EventType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CrawlActionsBuilder {

	private static final Logger LOG = LoggerFactory
			.getLogger(CrawlActionsBuilder.class);

	public static class ExcludeByParentBuilder {
		private final String tagName;
		private String id = null;
		private String clazz = null;

		private ExcludeByParentBuilder(String tagName) {
			this.tagName = tagName;
		}

		public void withId(String id) {
			this.id = id;
		}

		public void withClass(String clazz) {
			this.clazz = clazz;

		}

		private String asExcludeXpath(String identifier, String value) {
			return new StringBuilder().append("//")
					.append(tagName.toUpperCase()).append("[@")
					.append(identifier).append("='").append(value)
					.append("']//*").toString();
		}

		private ImmutableList<CrawlElement> asExcludeList(
				List<CrawlElement> includes) {
			String xpath;
			if (id != null) {
				xpath = asExcludeXpath("id", id);
			} else if (clazz != null) {
				xpath = asExcludeXpath("class", clazz);
			} else {
				xpath = "//" + tagName.toUpperCase() + "//*";
			}
			ImmutableList.Builder<CrawlElement> builder = ImmutableList
					.builder();
			for (CrawlElement include : includes) {
				builder.add(new CrawlElement(EventType.click, include
						.getTagName()).underXPath(xpath));
			}
			return builder.build();
		}
	}

	private final List<CrawlElement> crawlElements = Lists.newLinkedList();
	private final List<CrawlElement> crawlElementsExcluded = Lists
			.newLinkedList();
	private final List<ExcludeByParentBuilder> crawlParentsExcluded = Lists
			.newLinkedList();
	private ImmutableList<CrawlElement> resultingElementsExcluded = null;

	CrawlActionsBuilder() {
	}

	/**
	 * Set of HTML elements Crawljax will click during crawling For example 1)
	 * &gt;a.../&lt; 2) &lt;div/&gt; click("a") will only include 1 if clickOnce
	 * is true (default). This set can be restricted by
	 * {@link #dontClick(String)}.
	 *
	 * @param tagName the tag name of the elements to be included
	 * @return this CrawlElement
	 */
	public CrawlElement click(String tagName) {
		checkNotRead();
		Preconditions.checkNotNull(tagName, "Tag name cannot be null");
		CrawlElement crawlTag = new CrawlElement(EventType.click,
				tagName.toUpperCase());
		crawlElements.add(crawlTag);
		return crawlTag;
	}
	
	public CrawlElement enter(String tagName) {
		checkNotRead();
		Preconditions.checkNotNull(tagName, "Tag name cannot be null");
		CrawlElement crawlTag = new CrawlElement(EventType.enter,
				tagName.toUpperCase());
		crawlElements.add(crawlTag);
		return crawlTag;
	}

	/**
	 * Set of HTML elements Crawljax will click during crawling For example 1)
	 * &lt;a.../&gt; 2) &lt;div/&gt; click("a") will only include 1 This set can
	 * be restricted by {@link #dontClick(String)}. If no clicks are specified,
	 * {@link #clickDefaultElements()} is enabled.
	 *
	 * @param tagNames the tag name of the elements to be included
	 */
	public void click(String... tagNames) {
		for (String tagName : tagNames) {
			click(tagName);
		}
	}

	/**
	 * Specifies that Crawljax should click all the default clickable elements.
	 * These include: All anchor tags All buttons
	 */
	public void clickDefaultElements() {
		click("a");
		click("button");
		click("input").withAttribute("type", "submit");
		click("input").withAttribute("type", "button");
	}

	private void checkNotRead() {
		Preconditions.checkState(resultingElementsExcluded == null,
				"You cannot modify crawl actions once it's read");
	}

	/**
	 * Set of HTML elements Crawljax will NOT click during crawling When an HTML
	 * is present in the click and dontClick sets, then the element will not be
	 * clicked. For example: 1) &lt;a href="#"&gt;Some text&lt;/a&gt; 2) &lt;a
	 * class="foo" .../&gt; 3) &lt;div class="foo" .../&gt; click("a")
	 * dontClick("a").withAttribute("class", "foo"); Will include only include
	 * HTML element 2
	 *
	 * @param tagName the tag name of the elements to be excluded
	 * @return crawlTag the CrawlElement
	 */
	public CrawlElement dontClick(String tagName) {
		checkNotRead();
		Preconditions.checkNotNull(tagName, "Tag name cannot be null");
		CrawlElement crawlTag = new CrawlElement(EventType.click,
				tagName.toUpperCase());
		crawlElementsExcluded.add(crawlTag);
		return crawlTag;
	}

	/**
	 * Click no children of the specified parent element.
	 *
	 * @param tagName The tag name of which no children should be clicked.
	 * @return The builder to append more options.
	 */
	public ExcludeByParentBuilder dontClickChildrenOf(String tagName) {
		checkNotRead();
		Preconditions.checkNotNull(tagName);
		ExcludeByParentBuilder exclude = new ExcludeByParentBuilder(
				tagName.toUpperCase());
		crawlParentsExcluded.add(exclude);
		return exclude;
	}

	/**
	 * @return the crawlElements.
	 */
	private ImmutableList<CrawlElement> getCrawlElements() {
		if (crawlElements.isEmpty()) {
			clickDefaultElements();
		}
		return ImmutableList.copyOf(crawlElements);
	}

	/**
	 * @return the crawlElementsExcluded
	 */
	private ImmutableList<CrawlElement> getCrawlElementsExcluded() {
		synchronized (this) {
			if (resultingElementsExcluded == null) {
				ImmutableList.Builder<CrawlElement> builder = ImmutableList
						.builder();
				builder.addAll(crawlElementsExcluded);
				for (ExcludeByParentBuilder exclude : crawlParentsExcluded) {
					builder.addAll(exclude.asExcludeList(crawlElements));
				}
				resultingElementsExcluded = builder.build();
				LOG.debug("Excluded elements = {}", resultingElementsExcluded);
			}
			return resultingElementsExcluded;
		}
	}

	/**
	 * @return a {@link Pair} where {@link Pair#getLeft()} are the includes and
	 * {@link Pair#getRight()} are the excludes.
	 */
	Pair<ImmutableList<CrawlElement>, ImmutableList<CrawlElement>> build() {
		return Pair.of(getCrawlElements(), getCrawlElementsExcluded());
	}

}
