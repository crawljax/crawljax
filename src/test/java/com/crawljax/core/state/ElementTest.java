package com.crawljax.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ElementTest {

	@Test
	public void testSerliazibility() {
		String HTML =
		        "<SCRIPT src='js/jquery-1.2.1.js' type='text/javascript'></SCRIPT> "
		                + "<SCRIPT src='js/jquery-1.2.3.js' type='text/javascript'></SCRIPT>"
		                + "<body><div id='firstdiv' class='orange'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";
		StateVertex sv = new StateVertex("test", HTML);

		try {
			Node node = sv.getDocument().getElementById("thea");
			Element element = new Element(node);

			byte[] serialized = SerializationUtils.serialize(element);
			Element deserializedElement = (Element) SerializationUtils.deserialize(serialized);
			assertEquals(element, deserializedElement);
			assertEquals(element.getElementId(), deserializedElement.getElementId());

		} catch (SAXException e1) {
			fail(e1.getMessage());
		} catch (IOException e1) {
			fail(e1.getMessage());
		}
	}

}
