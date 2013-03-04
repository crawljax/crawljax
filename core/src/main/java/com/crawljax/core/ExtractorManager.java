package com.crawljax.core;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;

/**
 * This interface denotes all the operations a CandidateExtractor can execute.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public interface ExtractorManager {

	/**
	 * Check if a given element is already checked, preventing duplicate work.
	 * 
	 * @param element
	 *            the to search for if its already checked
	 * @return true if the element is already checked
	 */
	boolean isChecked(String element);

	/**
	 * Mark a given element as checked to prevent duplicate work. A elements is only added when it
	 * is not already in the set of checked elements.
	 * 
	 * @param candidateElement
	 *            the element that is checked
	 * @return true if !contains(candidateElement.uniqueString)
	 */
	boolean markChecked(CandidateElement candidateElement);

	/**
	 * increase the number of checked elements, as a statistics measure to know how many elements
	 * were actually examined.
	 */
	void increaseElementsCounter();

	/**
	 * Return internal counter for the examined elements.
	 * 
	 * @return the number of EximinedElements
	 */
	int numberOfExaminedElements();

	/**
	 * Retrieve the eventable condition checker.
	 * 
	 * @return the eventableConditionChecker that should be used during the candidate extraction.
	 */
	EventableConditionChecker getEventableConditionChecker();

	/**
	 * Check if one or more CrawlConditions matches the current state in the browser.
	 * 
	 * @param browser
	 *            the browser to execute in the CrawlConditions.
	 * @return true if one or more CrawlConditions stratifies or non is specified.
	 */
	boolean checkCrawlCondition(EmbeddedBrowser browser);

}
