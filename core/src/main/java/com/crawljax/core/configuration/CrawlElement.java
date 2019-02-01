package com.crawljax.core.configuration;

import com.crawljax.condition.Condition;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.forms.FormInput;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the HTML elements which should be crawled. It represents all the HTML elements in the
 * DOM that match the specified tag name. 1) &lt;a class="foo" .. 2) &lt;div... 3) &lt;a href=
 * "http:// CrawlElement exampleCrawlElement = new CrawlElement("a") represents the elements 1 and 3
 * You can refine the set of elements a CrawlElement represents by specifying attributes and XPath
 * conditions For example, you can refine exampleCrawlElement to represent only element 1 using
 * exampleCrawlElement.withAttribute("class", "foo");
 * <p>
 * NOTE: In general CrawlElement is not designed to be instantiated directly. CrawlElement should be
 * used as follows:
 * </p>
 * <p>
 * <code>
 * CrawljaxConfiguration crawler = new CrawljaxConfiguration();
 * crawler.click("a");
 * crawler.click("div").withAttribute("class", "clickable");
 * crawler.dontClick("a").withText("id", "logout");
 * crawler.dontClick("a").underXpath("//DIV[@id='header']");
 * </code>
 * </p>
 */
public final class CrawlElement {

	private final String tagName;
	private final List<Condition> conditions = Lists.newLinkedList();
	private final String id;
	private final EventType eventType;
	private final List<FormInput> inputFieldIds = Lists.newLinkedList();

	private String underXpath;

	/**
	 * To create a CrawlElement representing an HTML element &lt;a&gt;MyLink&lt;/a&gt; the tag name
	 * would be "a".
	 *
	 * @param eventType the event type for this crawl element.
	 */
	protected CrawlElement(EventType eventType, String tagName) {
		this.tagName = tagName.toUpperCase();
		this.id = "id" + hashCode();
		this.eventType = eventType;
	}

	/**
	 * Crawljax will crawl the HTML elements while crawling if and only if all the specified
	 * conditions are satisfied. IMPORTANT: only works with click()!!! For example:
	 * when(onContactPageCondition) will only click the HTML element if it is on the contact page
	 *
	 * @param conditions the condition to be met.
	 * @return this
	 */
	public CrawlElement when(Condition... conditions) {
		this.conditions.addAll(Arrays.asList(conditions));
		return this;
	}

	/**
	 * Restrict CrawlElement to include only HTML elements with the specified attribute. For example
	 * &lt;div class="foo"&gt;... &lt;div class="bar"&gt;.. &lt;/div&gt; &lt;/div&gt;
	 * withAttribute("class", "foo") Would restrict this CrawlElement to only include the div with
	 * class="foo"... AttributeName and value strings can support wild-card characters. Use % in to
	 * represent a wild-card string. e.g (% is the regex .*) When withAttribute() is called multiple
	 * times the CrawlElement will match only those HTML elements that have all the specified
	 * attributes.
	 *
	 * @param attributeName the name of the attribute
	 * @param value         the value of the attribute
	 * @return this CrawlElement
	 */
	public CrawlElement withAttribute(String attributeName, String value) {
		if (this.underXpath == null || this.underXpath.isEmpty()) {
			this.underXpath = "//" + this.tagName + "[@" + attributeName + "='" + value + "']";
		} else {
			this.underXpath = this.underXpath + " | " + "//" + this.tagName + "[@" + attributeName
					+ "='" + value + "']";
		}
		return this;
	}

	/**
	 * Restrict CrawlElement to include only HTML elements which are under HTML element X which
	 * identified by the xpath expression xpathExpression When xpathExpression returns no HTML
	 * elements this CrawlElement is not considered under element X. Element X is included when it
	 * matches the other restrictions of this CrawlElement For example: &lt;HTML&gt; &lt;BODY&gt;
	 * &lt;DIV id="foo"&gt; &lt;SPAN id="bar"&gt; &lt;A...&gt;&lt;/A&gt; &lt;/SPAN&gt; &lt;/DIV&gt;
	 * .. //DIV[@id='foo'] includes the div, span and a elements //SPAN[@id='foo'] includes the span
	 * and a elements //DIV[@class='link'] will not include any elements IMPORTANT: In the xpath
	 * expression write elements in uppercase and attributes in lowercase
	 *
	 * @param xpathExpression the xpath expression where the element should under
	 * @return This CrawlElement
	 */
	public CrawlElement underXPath(String xpathExpression) {
		this.underXpath = xpathExpression;
		return this;
	}

	/**
	 * Restrict CrawlElement to include only HTML elements which have the specified text For example 1)
	 * &lt;a&gt;Foo&lt;/a&gt; 2) &lt;a&gt;Bar&lt;/a&gt; 3) &lt;a&gt;Example 3a&lt;/a&gt;
	 * withText("Foo") will include element 1 withText("Example %") will include elements3 Text can
	 * support wild-card characters. Use % in to represent a wild-card string. e.g (% is the regex
	 * .*)
	 *
	 * @param text Text that should be inside the element.
	 * @return CrawlElement with text
	 */
	public CrawlElement withText(String text) {
		if (this.underXpath == null || this.underXpath.isEmpty()) {
			this.underXpath = "//" + this.tagName + "[text()=" + escapeApostrophes(text) + "]";
		} else {
			this.underXpath = this.underXpath + " | " + "//" + this.tagName + "[text()="
					+ escapeApostrophes(text) + "]";
		}
		return this;
	}

	/**
	 * @return the EventableCondition belonging to this CrawlElement
	 */
	public EventableCondition getEventableCondition() {
		if ((getWithXpathExpression() == null || getWithXpathExpression().equals(""))
				&& getConditions().isEmpty() && getInputFieldIds().isEmpty()) {
			return null;
		}
		EventableCondition eventableCondition = new EventableCondition(getId());
		if (getWithXpathExpression() != null && !getWithXpathExpression().equals("")) {
			eventableCondition.setInXPath(getWithXpathExpression());
		}
		if (getConditions().size() > 0) {
			eventableCondition.setConditions(getConditions());
		}
		if (getInputFieldIds().size() > 0) {
			eventableCondition.setLinkedInputFields(getInputFieldIds());
		}
		return eventableCondition;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("tagName", tagName)
				.add("conditions", conditions)
				.add("id", id).add("eventType", eventType).add("inputFieldIds", inputFieldIds)
				.add("underXpath", underXpath).toString();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return The tag name.
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * @return the withXpathExpression
	 */
	public String getWithXpathExpression() {
		return underXpath;
	}

	/**
	 * Sets the conditions.
	 *
	 * @param conditions The conditions
	 */
	protected void setConditions(Condition... conditions) {
		for (Condition condition : conditions) {
			this.conditions.add(condition);
		}
	}

	/**
	 * @return the conditions
	 */
	protected ImmutableList<Condition> getConditions() {
		return ImmutableList.copyOf(conditions);
	}

	/**
	 * @return the inputFieldIds
	 */
	protected ImmutableList<FormInput> getInputFieldIds() {
		return ImmutableList.copyOf(inputFieldIds);
	}

	/**
	 * @param formInputs Sets the list of input fields.
	 */
	protected void addInputFieldIds(List<FormInput> formInputs) {
		inputFieldIds.addAll(formInputs);
	}

	/**
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * Returns a string to resolve apostrophe issue in xpath
	 *
	 * @param text
	 * @return the apostrophe resolved xpath value string
	 */
	protected String escapeApostrophes(String text) {
		String resultString;
		if (text.contains("'")) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("concat('");
			stringBuilder.append(text.replace("'", "',\"'\",'"));
			stringBuilder.append("')");
			resultString = stringBuilder.toString();
		} else {
			resultString = "'" + text + "'";
		}
		return resultString;
	}

}
