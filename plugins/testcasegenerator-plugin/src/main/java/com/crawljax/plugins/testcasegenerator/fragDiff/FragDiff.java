package com.crawljax.plugins.testcasegenerator.fragDiff;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.crawljax.core.state.StatePair;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.plugins.testcasegenerator.TestSuiteHelper;
import com.crawljax.plugins.testcasegenerator.fragDiff.ImageAnnotation.AnnotationType;
import com.crawljax.plugins.testcasegenerator.report.MethodResult.WarnLevel;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.crawljax.vips_selenium.VipsUtils;

public class FragDiff {
	private static final Logger LOGGER = LoggerFactory.getLogger(FragDiff.class);
	
	private transient StatePair statePair;
	private transient FragmentManager fragmentManager;
	private static Color transparentRed = new Color(1f,0f,0f,.01f );
	
	private static Color transparentYellow = new Color(0f,1f,0f,.01f );
	
	private WarnLevel level = WarnLevel.LEVEL0;
	
	private String oldFile = null;
	
	private String newFile = null;
	
	private ImageAnnotations oldPageAnnotation = null;
	
	private ImageAnnotations newPageAnnotation = null;

	private String comp;

//	private Page newPage;

	public String getComp() {
		return comp;
	}



	public void setComp(String comp) {
		this.comp = comp;
	}



	public FragDiff(StatePair pair, FragmentManager fragmentManager, StateComparision comp) {
		this.statePair = pair;
		this.fragmentManager = fragmentManager;
		this.comp  = comp.name();
	}

	

	/**
	 * @return the old page with change labels.
	 * @throws IOException 
	 */
	public void annotateOldPage(boolean drawRectangles, File oldFile) throws IOException {
		boolean detectLevel = true;
		
		oldPageAnnotation = annotatePage(statePair.getState1(), statePair.getState2(), statePair.getState1Nodes(), detectLevel);
		if(drawRectangles) {
			annotatePage(oldPageAnnotation, statePair.getState1(), oldFile);
		}
	}

	/**
	 * @return the new page with change labels.
	 * @throws IOException 
	 */
	public void annotateNewPage(boolean drawRectangles, File newFile) throws IOException {
		boolean detectLevel = false;

		// Dont detect warn level using dynamic fragments for new page (not available)
		newPageAnnotation =	annotatePage(statePair.getState2(), statePair.getState1(), statePair.getState2Nodes(), detectLevel);
		if(drawRectangles) {
			annotatePage(newPageAnnotation, statePair.getState2(), newFile);
		}				
	}

	private void annotatePage(ImageAnnotations annotations, StateVertex stateVertex, File writeLocation) throws IOException {
		BufferedImage screenshot = ((HybridStateVertexImpl)stateVertex).getImage();
		BufferedImage overlayed = new BufferedImage(screenshot.getWidth(), screenshot.getHeight(), screenshot.getType());
		Graphics2D g2d = overlayed.createGraphics();
		g2d.drawImage(screenshot, 0, 0, null);
	
		for(ImageAnnotation annot: annotations.getAnnotations()) {
			Color color = getAnnotationColor(annot);
			g2d.setColor(color);
			Rectangle rect = annot.getRectangle();
			if(annot.isFill()) {
				g2d.fill(rect);
			}
			else {
				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
			}	
		}
		
		ImageIO.write(overlayed, "png", writeLocation);
	}



	private Color getAnnotationColor(ImageAnnotation annot) {
		Color color;
		if(annot.fill) {
			switch(annot.getType()) {
			case ADDED:
				color = transparentRed;
				break;
			case CHANGED:
				color = transparentRed;
				break;
			case DYNAMIC:
				color = transparentYellow;
				break;
			default:
				color = transparentYellow;
			}
		}
		else {
			switch(annot.getType()) {
			case ADDED:
				color = Color.black;
				break;
			case CHANGED:
				color = Color.red;
				break;
			case DYNAMIC:
				color = Color.blue;
			default:
				color = Color.green;
			}
		}
		return color;
	}
	
	
	public String getOldFile() {
		return oldFile;
	}



	public void setOldFile(String oldFile) {
		this.oldFile = oldFile;
	}



	public String getNewFile() {
		return newFile;
	}



	public void setNewFile(String newFile) {
		this.newFile = newFile;
	}



	public ImageAnnotations getOldPageAnnotation() {
		return oldPageAnnotation;
	}



	public void setOldPageAnnotation(ImageAnnotations oldPageAnnotation) {
		this.oldPageAnnotation = oldPageAnnotation;
	}



	public ImageAnnotations getNewPageAnnotation() {
		return newPageAnnotation;
	}



	public void setNewPageAnnotation(ImageAnnotations newPageAnnotation) {
		this.newPageAnnotation = newPageAnnotation;
	}



	/**
	 * Annotate the images with change labels.
	 * @param changedNodes 
	 */
	private ImageAnnotations annotatePage(StateVertex stateVertex, StateVertex toCompare, List<Node> changedNodes, boolean detectLevel) {
		ImageAnnotations annotations = new ImageAnnotations(stateVertex);
		// These nodes have chanaged characteristics according to APTED
		if(changedNodes != null) {
			for(Node node: changedNodes) {
				Rectangle rect = VipsUtils.getRectangle(node, null);
				if(rect.x <0 || rect.y<0 || rect.height <0 || rect.width <0) {
					rect = VipsUtils.getRectangle(node.getParentNode(), null);
//					if(rect.x <0 || rect.y<0 || rect.height <0 || rect.width <0) {
						continue;
//					}
				}
				// Draw black rectangle
				annotations.add(new ImageAnnotation(AnnotationType.ADDED, rect, false));
				
				// FIll red rectangle
				annotations.add(new ImageAnnotation(AnnotationType.ADDED, rect, true));
//				g2d.setColor(Color.black);
//				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
//				g2d.setColor(transparentRed);
//				g2d.fill(rect);
			}
		}
		
		List<Node> domDiffNodes;
		try {
			domDiffNodes = HybridStateVertexImpl.getDiffNodes(stateVertex.getDocument(), toCompare.getDocument(), TestSuiteHelper.APTED_VISUAL_DATA);
			
			if(domDiffNodes!=null && domDiffNodes.size() > 0 && detectLevel) {
				LOGGER.info("{} {} have textual differnces", stateVertex.getName(), toCompare.getName());
				setLevel(WarnLevel.LEVEL1);
			}
			
			for(Node node: domDiffNodes) {
				if(node.getNodeName().equalsIgnoreCase("#text")) {
					node = node.getParentNode();
				}
				Fragment closest= stateVertex.getClosestFragment(node);
				if(closest.getFragmentParentNode()!=null) {
					// Get the dynamic assignment from crawltime to the old page
					boolean isDynamic = VipsUtils.isDynamic(closest.getFragmentParentNode());
					closest.setDynamic(isDynamic);
				}
				Rectangle rect = VipsUtils.getRectangle(node, null);
				if(rect.x <0 || rect.y<0 || rect.height <0 || rect.width <0) {
						continue;
				}
				if(VipsUtils.isDynamic(node) || (closest!=null && closest.isDynamic())) {
					annotations.add(new ImageAnnotation(AnnotationType.DYNAMIC, rect, false));
					
					annotations.add(new ImageAnnotation(AnnotationType.DYNAMIC, rect, true));
					
				}
				else {
					if(detectLevel) {
						LOGGER.info("Diff Node is not dynamic. Setting warn level to 2 ");
						setLevel(WarnLevel.LEVEL2);
					}
					annotations.add(new ImageAnnotation(AnnotationType.CHANGED, rect, false));
					
					annotations.add(new ImageAnnotation(AnnotationType.CHANGED, rect, true));
	
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<Integer, Integer> mapping = fragmentManager.getLeafFragmentMapping(stateVertex, toCompare);
		
		
		List<Fragment> leafFragments = FragmentManager.getLeafFragments(stateVertex.getFragments());

		for(Integer i: mapping.keySet()) {
			if(mapping.get(i) < 0) {
				for(Fragment fragment: leafFragments) {
					if(fragment.getId() == i) {
						Rectangle rect = fragment.getRect();
//						g2d.setColor(Color.black);
//						g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
						
						annotations.add(new ImageAnnotation(AnnotationType.CHANGED, rect, true));
					}
				}
			}
			else if(i >=0 ) {
				for(Fragment fragment: leafFragments) {
//					annotateFragment(stateVertex.getRootFragment(), g2d, toCompare);
					if(fragment.getId() == i) {
						Rectangle rect = fragment.getRect();
//						g2d.setColor(Color.black);
//						g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
						
						annotations.add(new ImageAnnotation(AnnotationType.NONE, rect, true));
					}
				}
			}
		}
		
//		annotateFragment(stateVertex.getRootFragment(), g2d, toCompare);

		return annotations;

	}



	private boolean annotateFragment(Fragment fragment, Graphics2D g2d, StateVertex toCompare) {
		List<Fragment> duplicateFragments = new ArrayList<Fragment>();
		List<Fragment> equivalentFragments = new ArrayList<Fragment>();
		List<Fragment> differentFragments = new ArrayList<Fragment>();

			
		if(!FragmentManager.usefulFragment(fragment))
			return false;
//		
		boolean foundMatch = false;
		for(Fragment duplicate : fragmentManager.getDuplicateFragments(fragment)) {
			if(duplicate.getReferenceState().equals(toCompare)) {
				foundMatch = true;
				duplicateFragments.add(fragment);
				return true;
			}
		}
		
		if(foundMatch)
			return true;
		
		for(Fragment equivalent : fragmentManager.getEquivalentFragments(fragment)) {
			if(equivalent.getReferenceState().equals(toCompare)) {
				foundMatch = true;
				equivalentFragments.add(fragment);
//				g2d.setColor(transparentYellow);
//				g2d.fill(fragment.getRect());
				
				boolean allUseful = true;
				for(Fragment child: fragment.getChildren()) {
					allUseful = allUseful && annotateFragment(child, g2d, toCompare);
				}
				
				if(fragment.getChildren().isEmpty() || !allUseful) {
					g2d.setColor(transparentYellow);
					g2d.fill(fragment.getRect());
					return true;
				}
				
				return true;
			}
		}
		
		if(!foundMatch) {
			differentFragments.add(fragment);
//			g2d.setColor(transparentRed);
//			g2d.fill(fragment.getRect());	
			boolean allUseful = true;
			for(Fragment child: fragment.getChildren()) {
				allUseful = allUseful && annotateFragment(child, g2d, toCompare);
			}
			if(fragment.getChildren().isEmpty() || !allUseful) {
				g2d.setColor(transparentYellow);
				g2d.fill(fragment.getRect());
				return true;
			}
			return true;
		}
		
		return false;
	}



	public WarnLevel getLevel() {
		return level;
	}



	public void setLevel(WarnLevel level) {
		this.level = level;
	}
}
