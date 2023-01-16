/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructurejava
 */

package com.crawljax.vips_selenium;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.w3c.dom.Node;

/**
 * Class that represents visual structure.
 * @author Tomas Popela
 *
 */
public class VisualStructure {

	private List<Node> _nestedBlocks = null;
	private List<VisualStructure> _childrenVisualStructures = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	private int _width = 0;
	private int _height = 0;
	private int _x = 0;
	private int _y = 0;
	private int _doC = 12;
	private int _containImg = -1;
	private int _containP = -1;
	private int _textLength = -1;
	private int _linkTextLength = -1;
	private int _order;
	private boolean _containTable = false;
	private String _id = null;
	private int _tmpSrcIndex = 0;
	private int _srcIndex = 0;
	private int _minimalDoC = 0;
	private WebDriver driver = null;

	public VisualStructure(WebDriver driver)
	{
		this.driver = driver;
		_nestedBlocks = new ArrayList<Node>();
		_childrenVisualStructures = new ArrayList<VisualStructure>();
		_horizontalSeparators = new ArrayList<Separator>();
		_verticalSeparators = new ArrayList<Separator>();
	}

	/**
	 * @return Nested blocks in structure
	 */
	public List<Node> getNestedBlocks()
	{
		return _nestedBlocks;
	}

	/**
	 * Adds block to nested blocks
	 * @param nestedBlock New block
	 */
	public void addNestedBlock(Node nestedBlock)
	{
		this._nestedBlocks.add(nestedBlock);
	}

	/**
	 * Adds blocks to nested blocks
	 * @param nestedBlocks
	 */
	public void addNestedBlocks(List<Node> nestedBlocks)
	{
		this._nestedBlocks.addAll(nestedBlocks);
	}

	/**
	 * Sets blocks as nested blocks
	 * @param Nodes
	 */
	public void setNestedBlocks(List<Node> vipsBlocks)
	{
		this._nestedBlocks = vipsBlocks;
	}

	/**
	 * Clears nested blocks list
	 */
	public void clearNestedBlocks()
	{
		this._nestedBlocks.clear();
	}

	/**
	 * Removes nested block at given index
	 * @param index Index of block
	 */
	public void removeNestedBlockAt(int index)
	{
		this._nestedBlocks.remove(index);
	}

	/**
	 * Removes given child from structures children
	 * @param visualStructure Child
	 */
	public void removeChild(VisualStructure visualStructure)
	{
		this._childrenVisualStructures.remove(visualStructure);
	}

	/**
	 * Adds new child to visual structure children
	 * @param visualStructure New child
	 */
	public void addChild(VisualStructure visualStructure)
	{
		this._childrenVisualStructures.add(visualStructure);
	}

	/**
	 * Adds new child to visual structure at given index
	 * @param visualStructure New child
	 * @param index Index
	 */
	public void addChildAt(VisualStructure visualStructure, int index)
	{
		this._childrenVisualStructures.add(index, visualStructure);
	}

	/**
	 * Returns all children structures
	 * @return Children structures
	 */
	public List<VisualStructure> getChildrenVisualStructures()
	{
		return _childrenVisualStructures;
	}

	/**
	 * Sets visual structures as children of visual structure
	 * @param childrenVisualStructures List of visual structures
	 */
	public void setChildrenVisualStructures(List<VisualStructure> childrenVisualStructures)
	{
		this._childrenVisualStructures = childrenVisualStructures;
	}

	/**
	 * Returns all horizontal separators form structure
	 * @return List of horizontal separators
	 */
	public List<Separator> getHorizontalSeparators()
	{
		return _horizontalSeparators;
	}

	/**
	 * Sets list of separators as horizontal separators of structure
	 * @param horizontalSeparators List of separators
	 */
	public void setHorizontalSeparators(List<Separator> horizontalSeparators)
	{
		this._horizontalSeparators = horizontalSeparators;
	}

	/**
	 * Adds separator to horizontal separators of structure
	 * @param horizontalSeparator
	 */
	public void addHorizontalSeparator(Separator horizontalSeparator)
	{
		this._horizontalSeparators.add(horizontalSeparator);

	}

	/**
	 * Adds separators to horizontal separators of structure
	 * @param horizontalSeparators
	 */
	public void addHorizontalSeparators(List<Separator> horizontalSeparators)
	{
		this._horizontalSeparators.addAll(horizontalSeparators);

	}

	/**
	 * Returns X structure's coordinate
	 * @return X coordinate
	 */
	public int getX()
	{
		return this._x;
	}

	/**
	 * Returns structure's Y coordinate
	 * @return Y coordinate
	 */
	public int getY()
	{
		return this._y;
	}

	/**
	 * Sets X coordinate
	 * @param x X coordinate
	 */
	public void setX(int x)
	{
		this._x = x;
	}

	/**
	 * Sets Y coordinate
	 * @param y Y coordinate
	 */
	public void setY(int y)
	{
		this._y = y;
	}

	/**
	 * Sets width of visual structure
	 * @param width Width
	 */
	public void setWidth(int width)
	{
		this._width = width;
	}

	/**
	 * Sets height of visual structure
	 * @param height Height
	 */
	public void setHeight(int height)
	{
		this._height = height;
	}

	/**
	 * Returns width of visual structure
	 * @return Visual structure's width
	 */
	public int getWidth()
	{
		return this._width;
	}

	/**
	 * Returns height of visual structure
	 * @return Visual structure's height
	 */
	public int getHeight()
	{
		return this._height;
	}

	/**
	 * Returns list of all vertical separators in visual structure
	 * @return List of vertical separators
	 */
	public List<Separator> getVerticalSeparators()
	{
		return _verticalSeparators;
	}

	/**
	 * Sets list of separators as vertical separators of structure
	 * @param _verticalSeparators List of separators
	 */
	public void setVerticalSeparators(List<Separator> _verticalSeparators)
	{
		this._verticalSeparators = _verticalSeparators;
	}

	/**
	 * Adds separator to structure's vertical sepators
	 * @param verticalSeparator
	 */
	public void addVerticalSeparator(Separator verticalSeparator)
	{
		this._verticalSeparators.add(verticalSeparator);
	}

	/**
	 * Sets if of visual structure
	 * @param id Id
	 */
	public void setId(String id)
	{
		this._id = id;
	}

	/**
	 * Returns id of visual structure
	 * @return Visual structure's id
	 */
	public String getId()
	{
		return this._id;
	}

	/**
	 * Sets visual structure's degree of coherence DoC
	 * @param doC Degree of coherence - DoC
	 */
	public void setDoC(int doC)
	{
		this._doC = doC;
	}

	/**
	 * Returns structure's degree of coherence DoC
	 * @return Degree of coherence - DoC
	 */
	public int getDoC()
	{
		return _doC;
	}

	/**
	 * Finds minimal DoC in all children visual structures
	 * @param visualStructure Given visual structure
	 */
	private void findMinimalDoC(VisualStructure visualStructure)
	{
		if (!visualStructure.getId().equals("1"))
		{
			if (visualStructure.getDoC() < _minimalDoC)
				_minimalDoC = visualStructure.getDoC();
		}

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
		{
			findMinimalDoC(child);
		}
	}

	/**
	 * Updates DoC to normalized DoC
	 */
	public void updateToNormalizedDoC()
	{
		_doC = 12;

		for (Separator separator : _horizontalSeparators)
		{
			if (separator.normalizedWeight < _doC)
				_doC = separator.normalizedWeight;
		}

		for (Separator separator : _verticalSeparators)
		{
			if (separator.normalizedWeight < _doC)
				_doC = separator.normalizedWeight;
		}

		if (_doC == 12)
		{
			for (Node nestedBlock : _nestedBlocks)
			{
				if (VipsUtils.getDoC(nestedBlock) < _doC)
					_doC = VipsUtils.getDoC(nestedBlock);
			}
		}

		_minimalDoC = 12;

		findMinimalDoC(this);

		if (_minimalDoC < _doC)
			_doC = _minimalDoC;
	}

	/**
	 * Check if visual structure contain images
	 * @return Number of images
	 */
	public int containImg()
	{
		if (_containImg != -1)
			return _containImg;

		_containImg = 0;

		for (Node vipsBlock : _nestedBlocks)
		{
			_containImg += VipsUtils.getImageCount(vipsBlock);
		}

		return _containImg;
	}

	/**
	 * Check if visual structure contain paragraphs
	 * @return Nubmer of paragraphs
	 */
	public int containP()
	{
		if (_containP != -1)
			return _containP;

		_containP = 0;

		for (Node vipsBlock : _nestedBlocks)
		{
			_containP += VipsUtils.getPCount(vipsBlock);//vipsBlock.containP();
		}

		return _containP;
	}

	/**
	 * Checks visual structure contains table
	 * @return True if contains, otherwise false
	 */
	public boolean containTable()
	{
		if (_containTable)
			return _containTable;

		for (Node vipsBlock : _nestedBlocks)
		{
			if (VipsUtils.containsTable(vipsBlock))
					//vipsBlock.containTable())
			{
				_containTable = true;
				break;
			}
		}

		return _containTable;
	}

	/**
	 * Checks if visual structure is image
	 * @return True if is image, otherwise false
	 */
	public boolean isImg()
	{
		if (_nestedBlocks.size() != 1)
			return false;

		return VipsUtils.checkIsImg(_nestedBlocks.get(0));//.isImg();
	}

	/**
	 * Returns length of text in visual structure
	 * @return Text length
	 */
	public int getTextLength()
	{
		if (_textLength != -1)
			return _textLength;

		_textLength = 0;
		for (Node vipsBlock : _nestedBlocks)
		{
			_textLength += VipsUtils.getTextLength(vipsBlock);//vipsBlock.getTextLength();
		}

		return _textLength;
	}

	/**
	 * Returns length of text in links in visual structure
	 * @return Link text length
	 */
	public int getLinkTextLength()
	{
		if (_linkTextLength != -1)
			return _linkTextLength;

		_linkTextLength = 0;
		for (Node vipsBlock : _nestedBlocks)
		{
			_linkTextLength += VipsUtils.getLinkTextLength(vipsBlock);//vipsBlock.getLinkTextLength();
		}

		return _linkTextLength;
	}

	/**
	 * Gets visual structure font size
	 * @return Font size
	 */
	public int getFontSize()
	{
		if (_nestedBlocks.size() > 0)
			return (VipsUtils.getFontSize(_nestedBlocks.get(0), driver ));//_nestedBlocks.get(0).getFontSize();
		else
			return -1;
	}

	/**
	 * Gets visual structure font weight
	 * @return Font weight
	 */
	public String getFontWeight()
	{
//		if (_nestedBlocks.size() > 0)
//			return VipsUtils.getCssProperty(_nestedBlocks.get(0), "fontweight");//_nestedBlocks.get(0).getFontWeight();
//		else
			return "undef";
	}

	/**
	 * Gets visual structure background color
	 * @return Background color
	 */
	public String getBgColor()
	{
//		if (_nestedBlocks.size() > 0)
//			return VipsUtils.getCssProperty(_nestedBlocks.get(0), "bgcolor");
//					//_nestedBlocks.get(0).getBgColor();
//		else
			return "undef";
	}

	/**
	 * Gets frame source index of visual structure
	 * @return Frame source index
	 */
	public int getFrameSourceIndex()
	{
		if (_nestedBlocks.size() > 0)
			return -1;//_nestedBlocks.get(0).getFrameSourceIndex();
		else
			return -1;
	}

	/**
	 * Sets source index of visual structure
	 * @param node Node
	 * @param nodeToFind Node to find
	 */
//	private void setSourceIndex(Node node, Node nodeToFind)
//	{
//		if (!nodeToFind.equals(node))
//			_tmpSrcIndex++;
//		else
//			_srcIndex = _tmpSrcIndex;
//
//		for (int i = 0; i < node.getChildNodes().getLength(); i++)
//		{
//			setSourceIndex(node.getChildNodes().item(i), nodeToFind);
//		}
//	}

	/**
	 * Gets source index of visual strucure
	 * @return Visual structure's source index
	 */
//	public String getSourceIndex()
//	{
//		String sourceIndex = "";
//
//		if (_childrenVisualStructures.size() > 0)
//		{
//			setSourceIndex(_nestedBlocks.get(0).getBox().getNode().getOwnerDocument(), _nestedBlocks.get(0).getBox().getParent().getNode());
//			sourceIndex = String.valueOf(_srcIndex);
//		}
//		else
//		{
//			for (Node block : _nestedBlocks)
//			{
//				if (!sourceIndex.equals(""))
//					sourceIndex += ";";
//
//				sourceIndex += block.getSourceIndex();
//			}
//		}
//		return sourceIndex;
//	}

	/**
	 * Sets visual structure order
	 * @param order Order
	 */
	public void setOrder(int order)
	{
		this._order = order;
	}

	/**
	 * Returns visual structure order
	 * @return Visual structure order
	 */
	public int getOrder()
	{
		return _order;
	}

	/**
	 * Adds list of separators to visual structure vertical separators list.
	 * @param verticalSeparators
	 */
	public void addVerticalSeparators(List<Separator> verticalSeparators)
	{
		this._verticalSeparators.addAll(verticalSeparators);
	}
}
