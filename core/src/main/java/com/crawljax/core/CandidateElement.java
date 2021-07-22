package com.crawljax.core;

import java.util.List;

import org.w3c.dom.Element;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.forms.FormInput;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.util.DomUtils;
import com.crawljax.vips_selenium.VipsUtils;
import com.crawljax.vips_selenium.VipsUtils.AccessType;
import com.crawljax.vips_selenium.VipsUtils.Coverage;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Candidate element for crawling. It is possible to link this {@link Eventable} to form inputs, so
 * that Crawljax knows which values to set for this elements before it is clicked.
 */
public class CandidateElement {

	private final Identification identification;

	private final Element element;

	public Fragment getClosestFragment() {
		return closestFragment;
	}

	public void setClosestFragment(Fragment closestFragment) {
		this.closestFragment = closestFragment;
	}

	private final ImmutableList<FormInput> formInputs;
	private final String relatedFrame;

	private EventableCondition eventableCondition;
	
	transient Fragment closestFragment = null;
	transient Fragment closestDomFragment = null;

	
	private int duplicateAccess = 0;
	
	public int getDuplicateAccess() {
		return duplicateAccess;
	}

	public void setDuplicateAccess(int duplicateAccess) {
//		VipsUtils.setAccessType(element, AccessType.equivalent);
		VipsUtils.setCoverage(element, AccessType.equivalent, Coverage.action);
		this.duplicateAccess = duplicateAccess;
	}

	public int getEquivalentAccess() {
		return equivalentAccess;
	}

	public void setEquivalentAccess(int equivalentAccess) {
//		VipsUtils.setAccessType(element, AccessType.equivalent);
		VipsUtils.setCoverage(element, AccessType.equivalent, Coverage.action);

		this.equivalentAccess = equivalentAccess;
	}

	private int equivalentAccess = 0;
	
	private boolean directAccess = false;

	public boolean isDirectAccess() {
		return directAccess;
	}

	public void setDirectAccess(boolean directAccess) {
//		VipsUtils.setAccessType(element, AccessType.direct);
		VipsUtils.setCoverage(element, AccessType.direct, Coverage.action);

		this.directAccess = directAccess;
		incrementDuplicateAccess();		
//		incrementEquivalentAccess();
	}
	
	private EventType eventType = EventType.click;

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * Constructor for a element a identification and a relatedFrame.
	 *
	 * @param element        the element.
	 * @param identification the identification.
	 * @param relatedFrame   the frame this element belongs to.
	 */
	public CandidateElement(Element element, Identification identification, String relatedFrame,
			List<FormInput> formInputs) {
		this.identification = identification;
		this.element = element;
		this.relatedFrame = relatedFrame;
		this.formInputs = ImmutableList.copyOf(formInputs);
	}

	/**
	 * Constructor for a element a xpath-identification and no relatedFrame.
	 *
	 * @param element    the element
	 * @param xpath      the xpath expression of the element
	 * @param formInputs the input data to be used for the form
	 */
	public CandidateElement(Element element, String xpath, List<FormInput> formInputs) {
		this(element, new Identification(Identification.How.xpath, xpath), "", formInputs);
	}

	public CandidateElement(Element sourceElement, Identification identification,
			String relatedFrame) {
		this(sourceElement, identification, relatedFrame, ImmutableList.of());
	}

	/**
	 * @return unique string without atusa attribute
	 */
	public String getGeneralString() {
		ImmutableSet<String> exclude = ImmutableSet.of("atusa");

		StringBuilder result = new StringBuilder();
		if (element != null) {
			result.append(this.element.getNodeName()).append(": ");

		}
		result.append(DomUtils.getElementAttributes(this.element, exclude)).append(' ')
				.append(this.identification).append(' ').append(relatedFrame);

		return result.toString();
	}

	/**
	 * @return unique string of this candidate element
	 */
	public String getUniqueString() {

		String result = "";

		if (element != null) {
			result +=
					this.element.getNodeName() + ": "
							+ DomUtils.getAllElementAttributes(this.element) + " ";
		}

		result += this.identification + " " + relatedFrame;

		return result;
	}

	/**
	 * @return the element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * @return list with related formInputs
	 */
	public List<FormInput> getFormInputs() {
		return formInputs;
	}

	/**
	 * @param eventableCondition the EventableCondition
	 */
	public void setEventableCondition(EventableCondition eventableCondition) {
		this.eventableCondition = eventableCondition;
	}

	/**
	 * @return the identification object.
	 */
	public Identification getIdentification() {
		return identification;
	}

	/**
	 * @return the relatedFrame
	 */
	public String getRelatedFrame() {
		return relatedFrame;
	}

	/**
	 * Check all eventable Condition for correctness.
	 *
	 * @param browser the current browser instance that contains the current dom
	 * @return true if all conditions are satisfied or no conditions are specified
	 * @see #eventableCondition
	 * @see EventableCondition#checkAllConditionsSatisfied(EmbeddedBrowser)
	 */
	public boolean allConditionsSatisfied(EmbeddedBrowser browser) {
		if (eventableCondition != null) {
			return eventableCondition.checkAllConditionsSatisfied(browser);
		}
		// No condition specified so return true....
		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("identification", identification)
				.add("element", element)
				.add("formInputs", formInputs)
				.add("eventableCondition", eventableCondition)
				.add("relatedFrame", relatedFrame)
				.add("duplicateAccess", duplicateAccess)
				.add("equivalentAccess", equivalentAccess)
				.toString();
	}

	public boolean wasExplored() {
		if((isDirectAccess()) || (duplicateAccess>0) || equivalentAccess >0)
			return true;
		
		return false;
	}

	public void setClosestDomFragment(Fragment closestDom) {
		this.closestDomFragment = closestDom;
	}

	public Fragment getClosestDomFragment() {
		return closestDomFragment;
	}

	public void incrementDuplicateAccess() {
		duplicateAccess = duplicateAccess +1;
		equivalentAccess = equivalentAccess +1;
	}
	public void incrementEquivalentAccess() {
		equivalentAccess = equivalentAccess +1;
	}
	
}
