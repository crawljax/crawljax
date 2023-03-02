package com.crawljax.vips_selenium;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class DomUtils {

    /**
     *
     * @param dom the DOM document.
     * @return a string representation of the DOM.
     * @throws IOException exception if error transforming dom
     */
    public static String getDocumentToString(Document dom) throws IOException {
        try {
            Source source = new DOMSource(dom);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            throw new IOException("Could not transform the DOM", e);
        }
    }

    /**
     * transforms a string into a Document object. TODO This needs more optimizations. As it seems the
     * getDocument is called way too much times causing a lot of parsing which is slow and not
     * necessary.
     *
     * @param html the HTML string.
     * @return The DOM Document version of the HTML string.
     * @throws java.io.IOException if an IO failure occurs.
     */
    public static Document asDocument(String html) throws IOException {
        DOMParser domParser = new DOMParser();
        try {
            domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
            domParser.setFeature("http://xml.org/sax/features/namespaces", false);
            domParser.parse(new InputSource(new StringReader(html)));
        } catch (SAXException e) {
            throw new IOException("Error while reading HTML: " + html, e);
        }
        return domParser.getDocument();
    }
}
