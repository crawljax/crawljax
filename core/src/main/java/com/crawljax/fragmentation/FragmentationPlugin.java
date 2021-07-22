package com.crawljax.fragmentation;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlPathInfo;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.Crawler;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnRevisitStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StatePair;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.crawljax.util.DomUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

public class FragmentationPlugin implements OnNewStatePlugin, OnRevisitStatePlugin, PostCrawlingPlugin {
	private static final Logger LOG = LoggerFactory.getLogger(FragmentationPlugin.class);
	public static boolean COMPARE_FAST = false;
	private static boolean exportFragments = true;
	public static boolean DISABLE_STATE_COMP = false;

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {
		FragmentManager manager = context.getFragmentManager();
		EmbeddedBrowser browser = context.getBrowser();	
		File outputDir =  context.getConfig().getOutputDir();
		

		if(newState instanceof HybridStateVertexImpl) {
			fragmentState(newState, manager, browser, outputDir, false);
		}
	}
	
	public static void loadFragmentState(StateVertex fragState, FragmentManager manager, Document dom, BufferedImage screenshot) {

		Document fragmentedDom = ((HybridStateVertexImpl)fragState).loadFragmentDom(dom, screenshot);
		
		if(manager!=null) {
			for(Fragment fragment: fragState.getFragments()) {
				try {
					manager.addFragment(fragment, COMPARE_FAST);
					if(!fragment.isGlobal() && fragment.getDuplicateFragments().isEmpty()) {
						LOG.error("Fragment disconnected in :" + fragState.getName());
					}
				}catch(Exception ex) {
					LOG.error("Error adding fragment to fragment manager !!");
					manager.addFragment(fragment, COMPARE_FAST);
				}
			}
		}
	
	}

	public static void fragmentState(StateVertex newState, FragmentManager manager, EmbeddedBrowser browser, File outputDir, boolean exportDom) {
		long start = System.currentTimeMillis();
		File screenshotsFolder = new File(outputDir, "screenshots");
		if (!screenshotsFolder.exists()) {
		    boolean created = screenshotsFolder.mkdir();
		    checkArgument(created, "Could not create screenshotsFolder dir");
		}
//			File screenshotFile = new File(screenshotsFolder, "frag_" + newState.getName() + ".png");
		BufferedImage screenshot;
		if(((HybridStateVertexImpl)newState).getImage() != null) {
			screenshot = ((HybridStateVertexImpl)newState).getImage();
		}
		else {
			screenshot = browser.getScreenShotAsBufferedImage(500);
		}
		Document fragmentedDom = ((HybridStateVertexImpl)newState).fragmentDom(browser.getWebDriver(), screenshot, screenshotsFolder);
	
		long end = System.currentTimeMillis();
		LOG.info("Took {} ms to fragment dom ", end-start);
		start = System.currentTimeMillis();
		// Export during post crawl instead of here.
		if(exportDom)
			exportFragDom((HybridStateVertexImpl)newState, outputDir);
		
		
		addFragments(newState, manager);
		
		if(!exportFragments )
			return;
		
		
		try {
		((StateVertexImpl)newState).exportFragments(screenshotsFolder, screenshot);
		}catch(Exception ex) {
			LOG.error("Could not export Fragments for {}", newState.getName());
			LOG.error(ex.getMessage());
		}
		end = System.currentTimeMillis();
		LOG.info("Took {} ms to add fragments ", end-start);
		
		if(DISABLE_STATE_COMP) {
			return;
		}
		
		start = System.currentTimeMillis();
		
		
		try {
			manager.cacheStateComparisions(newState);
		}catch(Exception ex) {
			LOG.error("Could not Finish state comparision for {}", newState.getName());
			LOG.error(ex.getMessage());
		}
		end = System.currentTimeMillis();
		LOG.info("Took {} ms to state compare ", end-start);
	}

	public static void addFragments(StateVertex newState, FragmentManager manager) {
		if(newState.getFragments() == null) {
			return;
		}
		
		if(manager!=null) {
			for(Fragment fragment: newState.getFragments()) {
				try {
					if(!FragmentManager.usefulFragment(fragment)) {continue;}
					manager.addFragment(fragment, COMPARE_FAST);
					if(!fragment.isGlobal() && fragment.getDuplicateFragments().isEmpty()) {
						LOG.error("Fragment disconnected in :" + newState.getName());
					}
				}catch(Exception ex) {
					LOG.error("Error adding fragment to fragment manager !!");
//					manager.addFragment(fragment);
				}
			}
		}
	}

	private static void exportFragDom(HybridStateVertexImpl state, File outputDir) {
		
		Document fragmentedDom = state.getDocument();
		
		File domFolder = new File(outputDir, "doms");

		File domFile = new File(domFolder, "frag_" + state.getName() + ".html");

		if (!domFolder.exists()) {
		    boolean created = domFolder.mkdir();
		    checkArgument(created, "Could not create screenshotsFolder dir");
		}
		try {
			DomUtils.writeDocumentToFile(fragmentedDom, domFile.getAbsolutePath(), "html", 2);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onRevisitState(CrawlerContext context, StateVertex currentState) {
		
	}

	@Override
	public void postCrawling(CrawlSession session, ExitStatus exitReason) {
		File outputDir = session.getConfig().getOutputDir();

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.setPrettyPrinting().create();

		File CrawlPathsJson = new File(outputDir, "CrawlPaths.json");
		try {
			FileWriter writer = new FileWriter(CrawlPathsJson);
			gson.toJson(session.getCrawlPaths(), writer);
			writer.flush();
			writer.close();
			LOG.info("Wrote crawlpaths to CrawlPaths.json");
		}catch(Exception ex) {
			LOG.error("Error exporting Crawlpaths");
		}
		
		CopyOnWriteArrayList<StateVertex> allStates = new CopyOnWriteArrayList<>(session.getStateFlowGraph().getAllStates());

		for(StateVertex state: allStates) {
			exportFragDom((HybridStateVertexImpl)state, outputDir);
		}
		
		if(!DISABLE_STATE_COMP) {

			for(StateVertex state1: allStates) {
				for(StateVertex state2: allStates) {
					if(state1.getId() >= state2.getId())
						continue;
					try {
						boolean assignDynamic = true;
						StateComparision comp = session.getFragmentManager().cacheStateComparision(state2, state1, assignDynamic);
						LOG.debug(state1.getName()+ " : " + state2.getName() + " are " + comp);
					}catch(Exception ex) {
						LOG.error("Could not compare states {} and {}", state1.getName(), state2.getName());
					}
				}
			}
			
			List<Set<StateVertex>> nearDuplicates = session.getFragmentManager().getNearDuplicates();
			
			List<List<String>> nearDuplicatesString = new ArrayList<List<String>>();
			for(Set<StateVertex> set: nearDuplicates) {
				List<String> newSet = new ArrayList<String>();
				for(StateVertex vertex: set) {
					newSet.add(vertex.getName());
				}
				Collections.sort(newSet);
				nearDuplicatesString.add(newSet);
			}
			
			Set<StatePair> cache = session.getFragmentManager().getStateComparisionCache();
			
			File cacheGson = new File(outputDir, "comparisionCache.json");
			try {
				FileWriter writer = new FileWriter(cacheGson);
				gson.toJson(cache, writer);
				writer.flush();
				writer.close();
				
			} catch (JsonIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			File nearDuplicatesJson = new File(outputDir, "nearDuplicates.json");
			try {
				FileWriter writer = new FileWriter(nearDuplicatesJson);
				gson.toJson(nearDuplicatesString, writer);
				writer.flush();
				writer.close();
				LOG.info("Exported found nearduplicates to nearDuplicates.json");
			} catch (Exception e) {
				LOG.error("Error exporting near-duplicate states" );
			}
			
		}
		
		
		
		
		List<Fragment> fragments = session.getFragmentManager().getAllFragments();
		
		List<FragmentOutput> fragmentOutputs = new ArrayList<FragmentOutput>();
		
		
		/* Disabled for Mutation Analysis 
		 * Originally intended to stop prioritization process
		 * */
		//session.getFragmentManager().stopCrawling();
		
		List<FragmentPair> output = new ArrayList<FragmentPair>();
		
		HashMap<Fragment, FragmentOutput> fragMap = new HashMap<>();		
		for(Fragment fragment : fragments) {
			
			try {
			FragmentOutput fragOutput = new FragmentOutput(fragment);
			fragmentOutputs.add(fragOutput);
			if(!(FragmentManager.usefulFragment(fragment))) {
				continue;
			}
			fragMap.put(fragment, fragOutput);
//			System.out.println(fragOutput);
			for(Fragment duplicate: fragment.getDuplicateFragments()) {
				FragmentOutput dupOutput = new FragmentOutput(duplicate);
				fragMap.put(duplicate, dupOutput);
			}
			}catch(Exception ex) {
				LOG.error("Cannot output fragment {} of {}", fragment.getId(), fragment.getReferenceState().getId());
			}
		}
		
		
		File globalFragsJson = new File(outputDir, "allGlobalFragments.json");
		try {
			FileWriter writer = new FileWriter(globalFragsJson);
			gson.toJson(fragmentOutputs, writer);
			writer.flush();
			writer.close();
			LOG.info("Wrote Fragment comparisions to allGlobalFragments.json");
		}catch(Exception ex) {
			LOG.error("Error exporting all Fragments");
		}
		List<CrawlPathInfo> crawlPathsInfo = new ArrayList<CrawlPathInfo>();
		Iterator<List<Eventable>> paths = session.getCrawlPaths().iterator();
		
		for(int i=0; i<session.getCrawlPaths().size(); i++ ) {
			List<Eventable> path = paths.next();
			if(path instanceof CrawlPath) {
				CrawlPath crawlPath = (CrawlPath)path;
				CrawlPathInfo pathInfo = new CrawlPathInfo(i+1, crawlPath.getBacktrackTarget(), crawlPath.isBacktrackSuccess(), crawlPath.isReachedNearDup());
				pathInfo.setPathString(Crawler.printCrawlPath(path, false));
				crawlPathsInfo.add(pathInfo);
			}
		}
		File crawlPathInfoJson = new File(outputDir, "crawlPathsInfo.json");
		try {
			FileWriter writer = new FileWriter(crawlPathInfoJson);
			gson.toJson(crawlPathsInfo, writer);
			writer.flush();
			writer.close();
			LOG.info("Wrote crawlpathsinfo to crawlPathsInfo.json");
		}catch(Exception ex) {
			LOG.error("Error exporting crawlPathsInfo");
		}
		
	}

	public static void writeCrawlPath(CrawlSession session, CrawlPath crawlPath, int id) {
		File outputDir = session.getConfig().getOutputDir();

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.setPrettyPrinting().create();

		File CrawlPathsJson = new File(outputDir, "CrawlPath" + id+ ".json");
		try {
			FileWriter writer = new FileWriter(CrawlPathsJson);
			gson.toJson(crawlPath, writer);
			writer.flush();
			writer.close();
			LOG.info("Wrote crawlPath {} to {}", id, CrawlPathsJson.getName());
		}catch(Exception ex) {
			LOG.error("Error exporting Crawlpaths");
		}
	}

}
