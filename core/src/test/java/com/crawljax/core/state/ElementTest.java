package com.crawljax.core.state;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ElementTest {

	@Test
	public void testSerializability() throws SAXException, IOException {
		String HTML =
		        "<SCRIPT src='js/jquery-1.2.1.js' type='text/javascript'></SCRIPT> "
		                + "<SCRIPT src='js/jquery-1.2.3.js' type='text/javascript'></SCRIPT>"
		                + "<body><div id='firstdiv' class='orange'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";
		StateVertex sv = new StateVertexImpl(0, "test", HTML);

		Node node = sv.getDocument().getElementById("thea");
		Element element = new Element(node);

		byte[] serialized = SerializationUtils.serialize(element);
		Element deserializedElement = (Element) SerializationUtils.deserialize(serialized);
		assertThat(element, is(deserializedElement));
		assertThat(element.getElementId(), is(deserializedElement.getElementId()));

	}

}
