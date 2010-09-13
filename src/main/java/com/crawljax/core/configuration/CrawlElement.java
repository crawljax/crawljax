package com.crawljax.core.configuration;

import com.crawljax.condition.Condition;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.state.Eventable.EventType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the HTML elements which should be crawled. It represents all the HTML elements in the
 * DOM that match the specified tag name. 1) <a class="foo" .. 2) <div... 3) <a href="http://
 * CrawlElement exampleCrawlElement = new CrawlElement("a") represents the elements 1 and 3 You can
 * refine the set of elements a CrawlElement represents by specifying attributes and XPath
 * conditions For example, you can refine exampleCrawlElement to represent only element 1 using
 * exampleCrawlElement.withAttribute("class", "foo");
 * <p/>
 * NOTE: In general CrawlElement is not designed to be instantiated directly. CrawlElement should be
 * used as follows:
 * <p/>
 * <code>
 * CrawlSpecification crawler = new CrawlSpecification();
 * crawler.click("a");
 * crawler.click("div").withAttribute("class", "clickable");
 * 
 * crawler.dontClick("a").withText("id", "logout");
 * crawler.dontClick("a").underXpath("//DIV[@id='header']");
 * </code>
 * 
 * @see CrawlSpecification
 * @author DannyRoest@gmail.com (Danny Roest)
 * @version $Id$
 */
public final class CrawlElement {

	private String tagName;
	private final List<CrawlAttribute> crawlAttributes = new ArrayList<CrawlAttribute>();
	private final List<Condition> conditions = new ArrayList<Condition>();
	private final String id;
	private String underXpath;
	private List<String> inputFieldIds = new ArrayList<String>();
	private final EventType eventType;

	/**
	 * To create a CrawlElement representing an HTML element <a>MyLink</a> the tag name would be
	 * "a".
	 * 
	 * @param eventType
	 *            the event type for this crawl element.
	 */
	protected CrawlElement(EventType eventType) {
		this.id = "id" + hashCode();
		this.eventType = eventType;
	}

	/**
	 * Restrict CrawlElement to include only HTML elements with the specified attribute. For example
	 * <div class="foo">... <div class="bar">.. </div> </div> withAtttribute("class", "foo") Would
	 * restrict this CrawlElement to only include the div with class="foo"... AttributeName and
	 * value strings can support wild-card characters. Use % in to represent a wild-card string. e.g
	 * (% is the regex .*) When withAttribute() is called multiple times the CrawlElement will match
	 * only those HTML elements that have all the specified attributes.
	 * 
	 * @param attributeName
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 * @return this CrawlElement
	 */
	public CrawlElement withAttribute(String attributeName, String value) {
		this.crawlAttributes.add(new CrawlAttribute(attributeName, value));
		return this;
	}

	/**
	 * Restrict CrawlElement to include only HTML elements which are under HTML element X which
	 * identified by the xpath expression xpathExpression When xpathExpression returns no HTML
	 * elements this Crawltag is not considred under element X. Element X is included when it
	 * matches the other restrictions of this CrawlElement For example: <HTML> <BODY> <DIV id="foo">
	 * <SPAN id="bar"> <A...></A> </SPAN> </DIV> .. //DIV[@id='foo'] includes the div, span and a
	 * elements //SPAN[@id='foo'] includes the span and a elements //DIV[@class='link'] will not
	 * include any elements IMPORTANT: In the xpath expression write elements in uppercase and
	 * attributes in lowercase
	 * 
	 * @param xpathExpression
	 *            the xpath expression where the element should under
	 * @return This CrawlElement
	 */
	public CrawlElement underXPath(String xpathExpression) {
		this.underXpath = xpathExpression;
		return this;
	}

	/**
	 * Restrict crawlTag to include only HTML elements which have the specified text For example 1)
	 * <a>Foo</a> 2) <a>Bar</a> 3) <a>Example 3a</a> withText("Foo") will include element 1
	 * withText("Example %") will include elements3 Text can support wild-card characters. Use % in
	 * to represent a wild-card string. e.g (% is the regex .*)
	 * 
	 * @param text
	 *            Text that should be inside the element.
	 * @return Crawltag with text
	 */
	public CrawlElement withText(String text) {
		this.crawlAttributes.add(new CrawlAttribute("innertext", text));
		return this;
	}

	/**
	 * Set name of the tag.
	 * 
	 * @param tagName
	 *            Name of the tag.
	 */
	protected void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * @return the EventableCondition belonging to this CrawlElement
	 */
	protected EventableCondition getEventableCondition() {
		if ((getWithXpathExpression() == null || getWithXpathExpression().equals(""))
		        && getConditions().size() == 0 && getInputFieldIds().size() == 0) {
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
		StringBuffer ret = new StringBuffer(getTagName() + ":{");
		int i = 0;
		for (CrawlAttribute crawlAttribute : getCrawlAttributes()) {
			if (i > 0) {
				ret.append("; ");
			}
			i++;
			ret.append(crawlAttribute.getName());
			ret.append("=");
			ret.append(crawlAttribute.getValue());
		}
		ret.append("}");
		if (getId() != null && !getId().equals("")) {
			ret.append("[");
			ret.append(getId());
			ret.append("]");
		}
		return ret.toString();
	}

	/**
	 * @return a Test string.
	 */
	protected String toTestString() {
		StringBuffer ret = new StringBuffer(toString());
		ret.append("\nXpath expression: ");
		ret.append(getWithXpathExpression());
		ret.append("\n");
		for (Condition condition : getConditions()) {
			ret.append(condition.toString());
			ret.append("\n");
		}
		ret.append("InputFieldIds: ");
		ret.append(getInputFieldIds());
		return ret.toString();
	}

	/**
	 * @return the crawlAttributes
	 */
	protected List<CrawlAttribute> getCrawlAttributes() {
		return crawlAttributes;
	}

	/**
	 * @param crawlAttribute
	 *            Adds a crawlattribute.
	 */
	protected void addCrawlAttribute(CrawlAttribute crawlAttribute) {
		crawlAttributes.add(crawlAttribute);
	}

	/**
	 * @return the id
	 */
	protected String getId() {
		return id;
	}

	/**
	 * @return The tag name.
	 */
	protected String getTagName() {
		return tagName;
	}

	/**
	 * @return the withXpathExpression
	 */
	protected String getWithXpathExpression() {
		return underXpath;
	}

	/**
	 * Sets the conditions.
	 * 
	 * @param conditions
	 *            The conditions
	 */
	protected void setConditions(Condition... conditions) {
		for (Condition condition : conditions) {
			this.conditions.add(condition);
		}
	}

	/**
	 * @return the conditions
	 */
	protected List<Condition> getConditions() {
		return conditions;
	}

	/**
	 * @return the inputFieldIds
	 */
	protected List<String> getInputFieldIds() {
		return inputFieldIds;
	}

	/**
	 * @param ids
	 *            Sets the list of input field ids.
	 */
	protected void setInputFieldIds(List<String> ids) {
		inputFieldIds = ids;
	}

	/**
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

}
