package com.crawljax.util;

import java.util.Stack;

/**
 * Class for making presenting HTML without changing it's structure.
 * 
 * @author Danny
 * @version $Id$
 */
public final class PrettyHTML {

	private PrettyHTML() {

	}

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(PrettyHTML.class.getName());

	/**
	 * Pretty print HTML string.
	 * 
	 * @param html
	 *            The HTML string.
	 * @param strIndent
	 *            The indentation string.
	 * @return The pretty HTML.
	 */
	public static String prettyHTML(String html, String strIndent) {
		String[] elements = html.split("<");
		StringBuffer prettyHTML = new StringBuffer();
		int indent = 0;
		// preparsing for not closing elements
		elements = fixElements(elements);

		for (String element : elements) {
			if (!element.equals("")) {
				element = element.trim();

				if (!element.startsWith("/")) {
					// open element
					prettyHTML.append(repeatString(strIndent, indent));
					String[] temp = element.split(">");
					prettyHTML.append("<" + temp[0].trim() + ">\n");

					// only indent if element is not a single element (like
					// <img src='..' />)
					if ((!temp[0].endsWith("/") || temp.length == 1)
					        && !temp[0].startsWith("!--")) {
						indent++;
					}

					// if there is text after the element, print it
					if (temp.length > 1 && !temp[1].trim().equals("")) {
						prettyHTML.append(repeatString(strIndent, indent));
						prettyHTML.append(temp[1].trim() + "\n");
					}
				} else {
					// close element
					indent--;
					prettyHTML.append(repeatString(strIndent, indent));
					prettyHTML.append("<" + element + "\n");
				}
				if (element.endsWith("/>")) {
					indent--;
				}
			}
		}
		return prettyHTML.toString();

	}

	/**
	 * @param html
	 *            The HTML string.
	 * @return Pretty HTML.
	 */
	public static String prettyHTML(String html) {
		return prettyHTML(html, "\t");
	}

	/**
	 * @param s
	 * @param number
	 * @return s repreated number of times
	 */
	private static String repeatString(String s, int number) {
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < number; i++) {
			ret.append(s);
		}
		return ret.toString();
	}

	/**
	 * @param openElement
	 * @param closeElement
	 * @return wheter element has a seperate closing element
	 */
	private static boolean elementsRelated(String openElement, String closeElement) {
		openElement = openElement.split(">")[0];
		openElement = openElement.split(" ")[0];
		closeElement = closeElement.split(">")[0];
		return closeElement.startsWith("/" + openElement);
	}

	/**
	 * @param stack
	 * @param element
	 * @return whether the element is open
	 */
	private static boolean elementIsOpen(Stack<String> stack, String element) {
		for (int i = stack.size() - 1; i >= 0; i--) {
			if (elementsRelated(stack.get(i), element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param element
	 * @return wheter the element is a single element (<foo ... />)
	 */
	private static boolean isSingleElement(String element) {
		return element.indexOf("/>") != -1;
	}

	/**
	 * @param elements
	 * @return list with elements with added closing elements if needed
	 */
	private static String[] fixElements(String[] elements) {
		Stack<String> stackElements = new Stack<String>();
		Stack<Integer> stackIndexElements = new Stack<Integer>();
		for (int i = 0; i < elements.length; i++) {
			elements[i] = elements[i].trim();
			String element = elements[i];
			if (!element.equals("") && !element.startsWith("!--") && !element.endsWith("-->")) {
				while (stackElements.size() > 0 && element.startsWith("/")
				        && !elementsRelated(stackElements.peek(), element)) {
					// found a close element which is not on top of stack,
					// thus fix
					if (elementIsOpen(stackElements, element)) {
						// the element is open --> close element on top of
						// stack
						int index = stackIndexElements.peek();
						if (!isSingleElement(elements[index])
						        && elements[index].lastIndexOf(">") != -1) {
							// close this element
							elements[index] =
							        elements[index]
							                .substring(0, elements[index].lastIndexOf(">"))
							                + "/"
							                + elements[index].substring(elements[index]
							                        .lastIndexOf(">"));
						}
						stackElements.pop();
						stackIndexElements.pop();
					} else {
						// element is closing element while element is not
						// open--> remove.
						elements[i] = "";
						element = "";
						break;
					}

				}
				if (!element.equals("")) {
					// open element
					if (!element.startsWith("/")) {
						// only add to stack if has an open and close element
						if (!isSingleElement(element)) {
							stackElements.push(element);
							stackIndexElements.push(i);
						}
					} else {
						// close element, pop from stack if possible
						if (stackElements.size() > 0) {
							stackElements.pop();
							stackIndexElements.pop();
						}
					}
				}
			}
		}
		return elements;
	}

}
