package com.crawljax.vips_selenium;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.crawljax.util.XPathHelper;
/**
 * Class, that handles output of VIPS algorithm.
 * @author Tomas Popela
 *
 */
public final class VipsOutput {
	private static final Logger LOG = LoggerFactory.getLogger(VipsOutput.class);

	private Document doc = null;
	private boolean _escapeOutput = true;
	private int _pDoC = 0;
	private int _order = 1;
	private File _filename = null;
	boolean fragOutput = true;

	public VipsOutput() {
	}

	public VipsOutput(int pDoC, File fileName, boolean fragOutput) {
	
		this.setPDoC(pDoC);
		this._filename = fileName;
		this.fragOutput = fragOutput;
	}

	/**
	 * Gets source code of visual structure nodes
	 * @param elementBox Given node
	 * @return Source code
	 */
	private String getSource(Node elementBox)
	{
		String content = "";
		try
		{
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(elementBox), new StreamResult(buffer));
			content = buffer.toString().replaceAll("\n", "");
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	/**
	 * Append node from given visual structure to parent node
	 * @param parentNode Given visual structure
	 * @param visualStructure Parent node
	 */
	private void writeVisualBlocks(Element parentNode, VisualStructure visualStructure, int level)
	{
		Element layoutNode = doc.createElement("LayoutNode");

		
		layoutNode.setAttribute("FrameSourceIndex", String.valueOf(visualStructure.getFrameSourceIndex()));
		layoutNode.setAttribute("SourceIndex", XPathHelper.getXPathExpression(visualStructure.getNestedBlocks().get(0)));
		layoutNode.setAttribute("DoC", String.valueOf(visualStructure.getDoC()));
		layoutNode.setAttribute("ContainImg", String.valueOf(visualStructure.containImg()));
		layoutNode.setAttribute("IsImg", String.valueOf(visualStructure.isImg()));
		layoutNode.setAttribute("ContainTable", String.valueOf(visualStructure.containTable()));
		layoutNode.setAttribute("ContainP", String.valueOf(visualStructure.containP()));
		layoutNode.setAttribute("TextLen", String.valueOf(visualStructure.getTextLength()));
		layoutNode.setAttribute("LinkTextLen", String.valueOf(visualStructure.getLinkTextLength()));
		Node parentBox = VipsUtils.getParentBox(visualStructure.getNestedBlocks());
		layoutNode.setAttribute("DOMCldNum", String.valueOf(parentBox.getChildNodes().getLength()));
		layoutNode.setAttribute("FontSize", String.valueOf(visualStructure.getFontSize()));
		layoutNode.setAttribute("FontWeight", String.valueOf(visualStructure.getFontWeight()));
		layoutNode.setAttribute("BgColor", visualStructure.getBgColor());
		layoutNode.setAttribute("ObjectRectLeft", String.valueOf(visualStructure.getX()));
		layoutNode.setAttribute("ObjectRectTop", String.valueOf(visualStructure.getY()));
		layoutNode.setAttribute("ObjectRectWidth", String.valueOf(visualStructure.getWidth()));
		layoutNode.setAttribute("ObjectRectHeight", String.valueOf(visualStructure.getHeight()));
		layoutNode.setAttribute("ID", visualStructure.getId());
		layoutNode.setAttribute("order", String.valueOf(_order));
		layoutNode.setAttribute("level", String.valueOf(level));

		_order++;

		VipsUtils.setVipsLevel(parentBox, level);
		
//		LOG.debug(XPathHelper.getXPathExpression(parentBox));
		
		if (_pDoC >= visualStructure.getDoC())
		{
			// continue segmenting
			if (visualStructure.getChildrenVisualStructures().size() == 0)
			{
				if (visualStructure.getNestedBlocks().size() > 0)
				{
					String src = "";
					String content = "";
					for (Node block : visualStructure.getNestedBlocks())
					{
						Node elementBox = block;

						if (elementBox == null)
							continue;

						if (!elementBox.getNodeName().equalsIgnoreCase("Xdiv") &&
								!elementBox.getNodeName().equalsIgnoreCase("Xspan"))
							src += getSource(elementBox);
						else
							src += elementBox.getTextContent().trim();

						content += elementBox.getTextContent().trim() + " ";

					}
//					layoutNode.setAttribute("SRC", src);
//					layoutNode.setAttribute("Content", content);
				}
			}

			parentNode.appendChild(layoutNode);

			for (VisualStructure child : visualStructure.getChildrenVisualStructures()) {
				writeVisualBlocks(layoutNode, child, level+1);
			}
		}
		else
		{
			// "stop" segmentation
			if (visualStructure.getNestedBlocks().size() > 0)
			{
				String src = "";
				String content = "";
				for (Node block : visualStructure.getNestedBlocks())
				{
					Node elementBox = block;

					if (elementBox == null)
						continue;

					if (!elementBox.getNodeName().equals("Xdiv") &&
							!elementBox.getNodeName().equals("Xspan"))
						src += getSource(elementBox);
					else
						src += elementBox.getTextContent().trim();

					content += elementBox.getTextContent().trim() + " ";

				}
				layoutNode.setAttribute("SRC", src);
				layoutNode.setAttribute("Content", content);
			}

			parentNode.appendChild(layoutNode);
		}
	}

	public int drawVisualStructure(VisualStructure visualStructure, Graphics2D g2d, List<VipsRectangle> vipsRectangles, int id, int parentId, WebDriver driver) {
		LOG.debug(visualStructure.getNestedBlocks().toString());
		
		Rectangle rect2 = new Rectangle(visualStructure.getX(),
				visualStructure.getY(), visualStructure.getWidth(),
				visualStructure.getHeight());
		LOG.debug("VIPS" + rect2);
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(1));
//		g2d.draw(rect2);
		
		
		
		Node vipsBlock = VipsUtils.getParentBox(visualStructure.getNestedBlocks());
		Rectangle finalRect = null;
		Rectangle boxRect = VipsUtils.getRectangle(vipsBlock, driver);
		if(boxRect == null) {
			boxRect = new Rectangle(rect2.x, rect2.y, rect2.height, rect2.width);
		}
		Rectangle rect1 = new Rectangle(boxRect.x,
				boxRect.y, boxRect.width,
				boxRect.height);
		g2d.setColor(Color.red);
		g2d.setStroke(new BasicStroke(1));
//		g2d.draw(rect1);
//		LOG.info("DOM:" + rect1);
		
		
		
		if(rect2.contains(rect1) || rect2.width*rect2.height<=0) {
			LOG.debug("DOM rectangle is contained within vips"  );
			finalRect = rect1;
			}
		if(rect1.contains(rect2) || rect1.width*rect1.height<=0) {
			LOG.debug("VIPS rectangle is contained within dom" );
			finalRect = rect2;
			}
		if(!rect2.contains(rect1) && !rect1.contains(rect2) && rect1.width*rect1.height>0 && rect2.width*rect2.height>0){
			LOG.debug("Overflowing rectangles");

			Rectangle rect3 = VipsUtils.getIntersectionRectangle(rect1, rect2);
			finalRect = rect3;
			LOG.debug("Intersection" + rect3);
			g2d.setColor(Color.blue);
			g2d.setStroke(new BasicStroke(1));
//			g2d.draw(rect3);
		}
		
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(5));
		g2d.draw(finalRect);
//		LOG.info("final" + finalRect);
		VipsRectangle me = new VipsRectangle(visualStructure.getNestedBlocks(), id, parentId, XPathHelper.getXPathExpression(vipsBlock), finalRect);
		vipsRectangles.add(me);
		int childId = id + 1; 
		for(VisualStructure child : visualStructure.getChildrenVisualStructures()) {
			childId = drawVisualStructure(child, g2d, vipsRectangles, childId, id, driver);
			childId = childId + 1;
		}
		return childId;
	}
	
	
	
	public List<VipsRectangle> exportVisualStructureToImage(VisualStructure visualStructure, BufferedImage pageViewport, File target, boolean fragOutput, WebDriver driver) {
		LOG.debug(visualStructure.getNestedBlocks().toString());
		BufferedImage image = new BufferedImage(pageViewport.getWidth(), pageViewport.getHeight(), pageViewport.getType());
		Graphics2D g2d = image.createGraphics();
		g2d.drawImage(pageViewport, 0, 0, null);
		g2d.setColor(Color.black);
		
		List<VipsRectangle> vipsRectangles = new ArrayList<VipsRectangle>();
		
		
		
		drawVisualStructure(visualStructure, g2d, vipsRectangles,  0, -1, driver);
//		VipsUtils.exportFragments(pageViewport, target, vipsRectangles);
//		System.out.println(target.getAbsolutePath());
		if(fragOutput)
			VipsUtils.saveToImage(image, target);
		
		return vipsRectangles;
	}

	
	
	/**
	 * Writes visual structure to output XML
	 * @param visualStructure Given visual structure
	 * @param pageViewport Page's viewport
	 */
	public void writeXML(VisualStructure visualStructure, BufferedImage pageViewport, String url, String title)
	{
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			doc = docBuilder.newDocument();
			Element vipsElement = doc.createElement("VIPSPage");

//			String pageTitle = pageViewport.getRootElement().getOwnerDocument().getElementsByTagName("title").item(0).getTextContent();

			vipsElement.setAttribute("Url", url);
			vipsElement.setAttribute("PageTitle", title);
			vipsElement.setAttribute("WindowWidth", String.valueOf(pageViewport.getWidth()));
			vipsElement.setAttribute("WindowHeight", String.valueOf(pageViewport.getHeight()));
//			vipsElement.setAttribute("PageRectTop", String.valueOf(pageViewport.getAbsoluteContentY()));
//			vipsElement.setAttribute("PageRectLeft", String.valueOf(pageViewport.getAbsoluteContentX()));
//			vipsElement.setAttribute("PageRectWidth", String.valueOf(pageViewport.getContentWidth()));
//			vipsElement.setAttribute("PageRectHeight", String.valueOf(pageViewport.getContentHeight()));
			vipsElement.setAttribute("neworder", "0");
//			vipsElement.setAttribute("order", String.valueOf(pageViewport.getOrder()));

			doc.appendChild(vipsElement);

			writeVisualBlocks(vipsElement, visualStructure, 0);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);

			if (_escapeOutput)
			{
				StreamResult result = new StreamResult(_filename);
				transformer.transform(source, result);
			}
			else
			{
				StringWriter writer = new StringWriter();
				transformer.transform(source, new StreamResult(writer));
				String result = writer.toString();

				result = result.replaceAll("&gt;", ">");
				result = result.replaceAll("&lt;", "<");
				result = result.replaceAll("&quot;", "\"");

				if(fragOutput) {
					FileWriter fstream = new FileWriter(_filename + ".xml");
					fstream.write(result);
					fstream.close();
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	

	/**
	 * Enables or disables output escaping
	 * @param value
	 */
	public void setEscapeOutput(boolean value)
	{
		_escapeOutput = value;
	}

	/**
	 * Sets permitted degree of coherence pDoC
	 * @param pDoC pDoC value
	 */
	public void setPDoC(int pDoC)
	{
		if (pDoC <= 0 || pDoC> 11)
		{
			System.err.println("pDoC value must be between 1 and 11! Not " + pDoC + "!");
			return;
		}
		else
		{
			_pDoC = pDoC;
		}
	}

	/**
	 * Sets output filename
	 * @param filename Filename
	 */
	public void setOutputFileName(File filename)
	{
		if (filename!=null)
		{
			_filename = filename;
		}

	}
}
