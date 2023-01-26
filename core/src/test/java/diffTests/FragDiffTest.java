package diffTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Provider;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.ExtractorManager;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexFactory;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;

@RunWith(MockitoJUnitRunner.class)

public class FragDiffTest {
	@Mock
	private Provider<InMemoryStateFlowGraph> graphProvider;
	
	@Mock
	private Provider<StateFlowGraph> sfgProvider;
	
	@Mock
	private EmbeddedBrowser browser;
	
	@Mock
	private ExtractorManager checker;
	
	@Mock
	private FormHandler handler;
	
	private InMemoryStateFlowGraph sfg;

	private Object LOG;
	

	private EventableConditionChecker eventableChecker;


	@Test
	public void testTreeDiffSimpleDOM() throws IOException {
		String docString = "<HTML><HEAD><META http-equiv=\"Content-Type\"" +
				" content=\"text/html; charset=UTF-8\"></HEAD><BODY><SPAN id=\"testdiv\"> <a></a>" +
				"</SPAN><DIV style=\"colour:#FF0000\"><H>Header</H></DIV></BODY></HTML>";
		Document doc1 = DomUtils.asDocument(docString);
		
		String docString2  = "<HTML><HEAD><META http-equiv=\"Content-Type\"" +
				" content=\"text/html; charset=UTF-8\"></HEAD><BODY><DIV id=\"testdiv\"> <a></a>" +
				"</DIV><DIV style=\"colour:#FF0000\"><H>Header</H></DIV></BODY></HTML>";
		
		Document doc2 = DomUtils.asDocument(docString2);
		
		double distance = HybridStateVertexImpl.computeDistance(doc1, doc2, false);
		
		assertEquals(distance, 1.0, 0.0);
		
		List<List<Node>> differentNodes = HybridStateVertexImpl.getChangedNodes(doc1, doc2, false);

		List<Node> addedNodes = differentNodes.get(0);
		List<Node> removedNodes = differentNodes.get(1);

		assertTrue(addedNodes.get(0).getNodeName().equalsIgnoreCase("span"));
		assertTrue(removedNodes.get(0).getNodeName().equalsIgnoreCase("div"));
	}


	private void loadHybridState(String domString, StateVertex state, FragmentManager fragmentManager, File screenshotFile) throws IOException {
				
		BufferedImage screenshot = ImageIO.read(screenshotFile);
		
		FragmentationPlugin.loadFragmentState(state, fragmentManager, DomUtils.asDocument(domString), screenshot);
	}
	
	
	
	
	public void treeDiff(StateVertex state1, StateVertex state2, FragmentManager manager) {
		boolean mapped = mapFragments(state1.getRootFragment(), state2.getRootFragment(), manager);
		System.out.println("Tree Diff : " + mapped);
	}
	
	private boolean mapFragments(Fragment frag1, Fragment frag2, FragmentManager manager) {
		return areND2(frag1, frag2, manager);
	}
	private CandidateElement getMatchingCandidate(List<CandidateElement> elements,
			String xpath) {
		CandidateElement element = null;
		for(CandidateElement elem: elements) {
			System.out.println(elem.getIdentification().getValue());
			
			if(elem.getIdentification().getValue().equalsIgnoreCase(xpath)) {
				element = elem;
			}
		}
		return element;
	}
	@Test
	public void testCandidateAddition() throws IOException {
		CrawljaxConfigurationBuilder configBuilder = CrawljaxConfiguration.builderFor("http://locahost/dummy.html");
		configBuilder.crawlRules().clickOnce(false);

		File state729 = new File("src/test/resources/crawls/frag_state610.html");
		File state802 = new File("src/test/resources/crawls/frag_state628.html");
		String docString1 = FileUtils.readFileToString(state729);
		String docString2 = FileUtils.readFileToString(state802);

		HybridStateVertexImpl state1= new HybridStateVertexImpl(0, "", "index", docString1, docString1, 0.0, false);
		
		sfg =  new InMemoryStateFlowGraph(new ExitNotifier(0), new HybridStateVertexFactory(0, configBuilder, false));


		FragmentManager manager = new FragmentManager(graphProvider);

		File screenshot1 = new File("src/test/resources/crawls/state652.png");
		loadHybridState(docString1, state1, manager, screenshot1);

		File screenshot2 = new File("src/test/resources/crawls/state653.png");

		
		when(checker.checkCrawlCondition(browser)).thenReturn(true);
		
		eventableChecker = new EventableConditionChecker(configBuilder.build().getCrawlRules());
		when(checker.getEventableConditionChecker()).thenReturn(eventableChecker);
		
		try {
			NodeList returnList = XPathHelper.evaluateXpathExpression(state1.getDocument(), "//A[text()='\\[A\\-Z\\]']");
			System.out.println(returnList.getLength());
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CandidateElementExtractor extractor = new CandidateElementExtractor(checker, browser, handler, configBuilder.build());
		
		List<CandidateElement> elements = extractor.extract(state1);
		LinkedList<CandidateElement> results = new LinkedList<>();
		results.addAll(elements);
		System.out.println(" Found elements " +  elements.size());
		
		
		state1.setElementsFound(results);
		
		CandidateElement element = getMatchingCandidate(results,"/HTML[1]/BODY[1]/TABLE[2]/TBODY[1]/TR[1]/TD[2]/FORM[1]/INPUT[2]" + 
				"");

		manager.recordAccess(element, state1);
		
		HybridStateVertexImpl state2= new HybridStateVertexImpl(1, "", "state1", docString2, docString2, 0.0, false);

		loadHybridState(docString2, state2, manager, screenshot2);
		
		state1.equals(state2);

		elements = extractor.extract(state2);
		results = new LinkedList<>();
		results.addAll(elements);
		System.out.println(" Found elements " +  elements.size());
		
		state2.setElementsFound(results);
		
		CandidateElement element2 = getMatchingCandidate(results,"/HTML[1]/BODY[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/FORM[1]/INPUT[1]");
		
		System.out.println(element2);
		
		manager.setAccess(state2);
		
		System.out.println(element2);
		System.out.println(element2.wasExplored());
		manager.recordAccess(element2, state2);
		
		
		StateComparision comp= manager.cacheStateComparision(state2, state1, true);
		
		System.out.println(DomUtils.getDocumentToString(state2.getDocument()));
//		StateComparision comp2=  manager.areND2(state2, state1);
//
		treeDiff(state1, state2, manager);
//		System.out.println("leaf frags " + comp2);
		System.out.println("using diff nodes " + comp);
		
	}
	
	@Test
	public void test2() throws IOException {
		CrawljaxConfigurationBuilder configBuilder = CrawljaxConfiguration.builderFor("http://locahost/dummy.html");

		File state729 = new File("src/test/resources/crawls/frag_state296.html");
		File state802 = new File("src/test/resources/crawls/frag_state297.html");
		String docString1 = FileUtils.readFileToString(state729);
		String docString2 = FileUtils.readFileToString(state802);

		HybridStateVertexImpl state1= new HybridStateVertexImpl(0, "", "index", docString1, docString1, 0.0, false);
		HybridStateVertexImpl state2= new HybridStateVertexImpl(1, "", "state1", docString2, docString2, 0.0, false);
		
		sfg =  new InMemoryStateFlowGraph(new ExitNotifier(0), new HybridStateVertexFactory(0, configBuilder, false));
		
//		when(graphProvider.get()).thenReturn(sfg);
		
//		when(sfgProvider.get()).thenReturn(sfg);

		FragmentManager manager = new FragmentManager(graphProvider);

		File screenshot1 = new File("src/test/resources/crawls/state296.png");
		loadHybridState(docString1, state1, manager, screenshot1);

		File screenshot2 = new File("src/test/resources/crawls/state297.png");
		loadHybridState(docString2, state2, manager, screenshot2);
		
		state1.equals(state2);

		StateComparision comp= manager.cacheStateComparision(state2, state1, true);
		
		System.out.println(DomUtils.getDocumentToString(state2.getDocument()));
		treeDiff(state1, state2, manager);

		BufferedImage state1Annotated = annotatePage(state1, false);
		BufferedImage state1Annotated2 = annotatePage(state2, false);

	}
	
	public boolean uniqueMapping(List<Fragment> newFragments, List<Fragment> expectedFragments, FragmentManager manager) {
		if(newFragments.isEmpty() && expectedFragments.isEmpty())
			return true;
		
		if(newFragments.size() != expectedFragments.size())
			return false;
		
		HashMap<Integer, Integer> mapping = new HashMap<>();
		
		boolean uniqueMap = true;
		boolean allMappingFound = true;

		for(Fragment newFragment : newFragments) {
//			System.out.println("new" + newFragment.getId());
			boolean mappingFound = false;
			boolean unique = true;
			for(Fragment oldFragment: expectedFragments) {
				if(manager.getRelatedFragments(newFragment).contains(oldFragment)) {
					if(mapping.containsValue(oldFragment.getId())) {
						mappingFound = true;
						unique = false;
						mapping.put(newFragment.getId(), oldFragment.getId());
					}
					else {
						if(!unique) {
							unique = true;
							mapping.remove(newFragment.getId());
						}
						mappingFound = true;
						System.out.println("mapped useless: " + newFragment.getId() + " " + oldFragment.getId());
						mapping.put(newFragment.getId(), oldFragment.getId());
						break;
					}
				}
			}
			allMappingFound = mappingFound && allMappingFound;
			uniqueMap = unique && uniqueMap;
		}
		
		if(!allMappingFound)
			return false;
		
		if(!uniqueMap)
			return false;
		else
			return true;
	}
	
	public boolean areND2(Fragment frag1, Fragment frag2, FragmentManager manager) {
		List<Fragment> newFragments = frag1.getChildren();
		
		List<Fragment> expectedFragments = frag2.getChildren();
		HashMap<Integer, Integer> mapping = new HashMap<>();
		
		
		
		boolean uniqueMap = true;
		boolean allMappingFound = true;
		List<Fragment> uselessFragments1 = new ArrayList<Fragment>();
		List<Fragment> uselessFragments2 = new ArrayList<Fragment>();

		for(Fragment newFragment : newFragments) {
			if(!FragmentManager.usefulFragment(newFragment)) {
				if(!uselessFragments1.contains(newFragment))
					uselessFragments1.add(newFragment);
				continue;
			}
			
//			System.out.println("new" + newFragment.getId());
			boolean mappingFound = false;
			boolean unique = true;
			for(Fragment oldFragment: expectedFragments) {
				if(!FragmentManager.usefulFragment(oldFragment)) {
					if(!uselessFragments2.contains(oldFragment))
						uselessFragments2.add(oldFragment);
					continue;
				}
				
				if(manager.getRelatedFragments(newFragment).contains(oldFragment)) {
					if(mapping.containsValue(oldFragment.getId())) {
						System.out.println("Repeat of mapping");
						mappingFound = true;
						unique = false;
						mapping.put(newFragment.getId(), oldFragment.getId());
					}
					else {
						if(!unique) {
							unique = true;
							mapping.remove(newFragment.getId());
						}
						mappingFound = true;
						System.out.println("mapped : " + newFragment.getId() + " " + oldFragment.getId());
						mapping.put(newFragment.getId(), oldFragment.getId());
						break;
					}
				}
			}
			allMappingFound = mappingFound && allMappingFound;
			uniqueMap = unique && uniqueMap;
		}
		
		boolean nd2 = false;
		
		if(!allMappingFound) {
			for(Fragment newFragment: newFragments) {
				if(!mapping.containsKey(newFragment.getId()) && FragmentManager.usefulFragment(newFragment)){
					for(Fragment oldFragment: expectedFragments) {
						boolean areND2 =  areND2(newFragment, oldFragment, manager);
						if(areND2) {
							mapping.put(newFragment.getId(), oldFragment.getId());
							System.out.println("ND2 Mapped : " + newFragment.getId() + " :  " + oldFragment.getId());
							nd2 = true;
						}
					}
				}
			}
			
		}

		if(mapping.isEmpty()) {
			return false;
		}
		boolean uselessMapped = false;
		if(uselessFragments1.size() == uselessFragments2.size()) {
			uselessMapped = uniqueMapping(uselessFragments1, uselessFragments2, manager);
			if(uselessMapped) {
				System.out.println("Useless mapped for "+ frag1.getId() + frag2.getId());
//				return true;
			}
		}
		
		if(!uniqueMap)
			return true;
		else
			return true;

	}
	
	private BufferedImage annotatePage(StateVertex stateVertex, boolean vips) {
		BufferedImage screenshot = ((HybridStateVertexImpl)stateVertex).getImage();
		BufferedImage overlayed = new BufferedImage(screenshot.getWidth(), screenshot.getHeight(), screenshot.getType());
		Graphics2D g2d = overlayed.createGraphics();
		g2d.drawImage(screenshot, 0, 0, null);
		
		
		for(Fragment fragment: stateVertex.getFragments()) {
			Rectangle rect = fragment.getRect();


			if(fragment.getFragmentParentNode() !=null) {
				if(vips) {
					continue;
				}
				else {
					g2d.setColor(Color.BLACK);
				}
			}
			else {
				if(vips)
					g2d.setColor(Color.BLACK);
				else
					continue;
			}
			g2d.drawString(""+fragment.getId(), rect.x-10, rect.y+10);

			g2d.draw(fragment.getRect());	
		}
		return overlayed;
	}
	
	public void showPicture(BufferedImage state1Annotated, BufferedImage state1Annotated2) throws InterruptedException{

		JLabel picLabel = new JLabel(new ImageIcon(state1Annotated));
		JFrame frame=new JFrame();  
        frame.add(picLabel);  
        frame.setSize(1200,890);  
        frame.setVisible(true);  
      
        JLabel picLabel2 = new JLabel(new ImageIcon(state1Annotated2));
		JFrame frame2=new JFrame();  
        frame2.add(picLabel2);  
        frame2.setSize(1200,890);  
        frame2.setVisible(true);  
        
        
		Object lock = new Object();
	    Thread t = new Thread() {
	        public void run() {
	            synchronized(lock) {
	                while (frame.isVisible() && frame2.isVisible())
	                    try {
	                        lock.wait();
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                System.out.println("Closing!!");
	            }
	        }
	    };
	    t.start();

	    frame.addWindowListener(new WindowAdapter() {

	        @Override
	        public void windowClosing(WindowEvent arg0) {
	            synchronized (lock) {
	            	frame.setVisible(false);
	            	frame2.setVisible(false);

	                lock.notify();
	            }
	        }

	    });

	    t.join();
	}

	
}
