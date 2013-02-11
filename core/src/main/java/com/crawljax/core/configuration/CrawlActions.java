package com.crawljax.core.configuration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.condition.Condition;
import com.crawljax.core.state.Eventable.EventType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Specifies the actions for CrawlElements NOTE: In general CrawlActions is not designed to be
 * instantiated directly. CrawlActions should be used via {@link CrawlSpecification} To add
 * conditions to check whether a tag should (not) be clicked one can use {@link #when(Condition...)}
 * . See also {@link Condition}
 */
public class CrawlActions {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlActions.class);

	public static class ExcludeByParentBuilder {
		private final String tagname;
		private String id = null;
		private String clasz = null;

		private ExcludeByParentBuilder(String tagname) {
			this.tagname = tagname;
		}

		public void withId(String id) {
			this.id = id;
		}

		public void withClass(String clasz) {
			this.clasz = clasz;

		}

		private String asExcludeXpath(String identifier, String value) {
			return new StringBuilder().append("//").append(tagname.toUpperCase()).append("[@")
			        .append(identifier).append("='").append(value).append("']//*").toString();
		}

		private ImmutableList<CrawlElement> asExcludeList(List<CrawlElement> includes) {
			String xpath;
			if (id != null) {
				xpath = asExcludeXpath("id", id);
			} else if (clasz != null) {
				xpath = asExcludeXpath("class", clasz);
			} else {
				xpath = "//" + tagname.toUpperCase() + "//*";
			}
			ImmutableList.Builder<CrawlElement> builder = ImmutableList.builder();
			for (CrawlElement include : includes) {
				builder.add(new CrawlElement(EventType.click, include.getTagName())
				        .underXPath(xpath));
			}
			return builder.build();
		}
	}

	private final List<CrawlElement> crawlElements = Lists.newLinkedList();
	private final List<CrawlElement> crawlElementsExcluded = Lists.newLinkedList();
	private final List<ExcludeByParentBuilder> crawlParentsExcluded = Lists.newLinkedList();

	private ImmutableList<CrawlElement> resultingElementsExcluded = null;

	CrawlActions() {
	}

	/**
	 * Set of HTML elements Crawljax will click during crawling For exmple 1) <a.../> 2) <div/>
	 * click("a") will only include 1 if clickOnce is true (default). This set can be restricted by
	 * {@link #dontClick(String)}.
	 * 
	 * @param tagName
	 *            the tag name of the elements to be included
	 * @return this CrawlElement
	 */
	public CrawlElement click(String tagName) {
		checkNotRead();
		Preconditions.checkNotNull(tagName, "Tagname cannot be null");
		CrawlElement crawlTag = new CrawlElement(EventType.click, tagName.toUpperCase());
		crawlElements.add(crawlTag);
		return crawlTag;
	}

	private void checkNotRead() {
		Preconditions.checkState(resultingElementsExcluded == null,
		        "You cannot modify crawlactions once it's read");
	}

	/**
	 * Set of HTML elements Crawljax will NOT click during crawling When an HTML is present in the
	 * click and dontClick sets, then the element will not be clicked. For example: 1) <a
	 * href="#">Some text</a> 2) <a class="foo" .../> 3) <div class="foo" .../> click("a")
	 * dontClick("a").withAttribute("class", "foo"); Will include only include HTML element 2
	 * 
	 * @param tagName
	 *            the tag name of the elements to be excluded
	 * @return crawlTag the CrawlElement
	 */
	public CrawlElement dontClick(String tagName) {
		checkNotRead();
		Preconditions.checkNotNull(tagName, "Tagname cannot be null");
		CrawlElement crawlTag = new CrawlElement(EventType.click, tagName.toUpperCase());
		crawlElementsExcluded.add(crawlTag);
		return crawlTag;
	}

	/**
	 * Click no childer of the specified parent element.
	 * 
	 * @param tagname
	 *            The tagname of which no children should be clicked.
	 * @return The builder to append more options.
	 */
	public ExcludeByParentBuilder dontClickChildrenOf(String tagname) {
		checkNotRead();
		Preconditions.checkNotNull(tagname);
		ExcludeByParentBuilder exclude = new ExcludeByParentBuilder(tagname.toUpperCase());
		crawlParentsExcluded.add(exclude);
		return exclude;
	}

	/**
	 * @return the crawlElements
	 */
	protected ImmutableList<CrawlElement> getCrawlElements() {
		return ImmutableList.copyOf(crawlElements);
	}

	/**
	 * @return the crawlElementsExcluded
	 */
	protected ImmutableList<CrawlElement> getCrawlElementsExcluded() {
		synchronized (this) {
			if (resultingElementsExcluded == null) {
				ImmutableList.Builder<CrawlElement> builder = ImmutableList.builder();
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

}
