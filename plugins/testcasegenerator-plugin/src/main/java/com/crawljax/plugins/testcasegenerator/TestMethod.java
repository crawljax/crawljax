/**
 * Created Jun 16, 2008
 */
package com.crawljax.plugins.testcasegenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mesbah
 * @version $Id: TestMethod.java 6234 2009-12-18 13:46:37Z mesbah $
 */
public class TestMethod {
	private String methodName;
	private List<TestMethodEvent> eventList;

	/**
	*
	*/
	public TestMethod() {
		super();
		eventList = new ArrayList<TestMethodEvent>();
	}

	/**
	 * @param methodName
	 *            the method name.
	 * @param eventList
	 *            the list of events.
	 */
	public TestMethod(String methodName, List<TestMethodEvent> eventList) {
		super();
		this.methodName = methodName;
		this.eventList = eventList;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName
	 *            the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the eventList
	 */
	public List<TestMethodEvent> getEventList() {
		return eventList;
	}

	public void addMethodEvent(TestMethodEvent methodEvent) {
		eventList.add(methodEvent);
	}

	/**
	 * @param eventList
	 *            the eventList to set
	 */
	public void setEventList(List<TestMethodEvent> eventList) {
		this.eventList = eventList;
	}
}
