package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a form configuration.
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 */
public class ReportConfiguration {

	private int reportMaxStateDifferences = 0;
	private List<String> reportDebugVariables = new ArrayList<String>();
	private boolean reportShowAllStateDifferences = true;
	private boolean reportIncludeScreenshots = true;

	/**
	 * @return the reportMaxStateDifferences
	 */
	public int getReportMaxStateDifferences() {
		return reportMaxStateDifferences;
	}

	/**
	 * @param number
	 *            the maximum number of state differences
	 */
	public void setReportMaxStateDifferences(int number) {
		this.reportMaxStateDifferences = number;
	}

	/**
	 * @return the reportDebugVariables
	 */
	public List<String> getReportDebugVariables() {
		return reportDebugVariables;
	}

	/**
	 * @param variables
	 *            the javascript variables or expression that should be displayed in the report.
	 */
	public void setReportDebugVariables(List<String> variables) {
		this.reportDebugVariables = variables;
	}

	/**
	 * @param variable
	 *            The debug variable to add.
	 */
	public void addReportDebugVariable(String variable) {
		reportDebugVariables.add(variable);
	}

	/**
	 * @return the reportShowAllStateDifferences
	 */
	public boolean getReportShowAllStateDifferences() {
		return reportShowAllStateDifferences;
	}

	/**
	 * @param value
	 *            whether to show all state differences without oracle comparators applied
	 */
	public void setReportShowAllStateDifferences(boolean value) {
		this.reportShowAllStateDifferences = value;
	}

	/**
	 * @return the reportIncludeScreenshots
	 */
	public boolean getReportIncludeScreenshots() {
		return reportIncludeScreenshots;
	}

	/**
	 * @param value
	 *            wheter to include screenshots in the report
	 */
	public void setReportIncludeScreenshots(boolean value) {
		this.reportIncludeScreenshots = value;
	}

}
