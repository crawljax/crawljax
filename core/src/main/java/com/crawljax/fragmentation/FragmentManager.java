package com.crawljax.fragmentation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Provider;
import javax.xml.xpath.XPathExpressionException;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StatePair;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.fragmentation.Fragment.FragmentComparision;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.crawljax.stateabstractions.visual.ColorHistogram;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.crawljax.vips_selenium.VipsUtils;
import com.crawljax.vips_selenium.VipsUtils.AccessType;
import com.crawljax.vips_selenium.VipsUtils.Coverage;

public class FragmentManager {
	private static  Logger LOG = LoggerFactory.getLogger(FragmentManager.class);

	public static FragmentRules USEFUL_FRAGMENT_RULES = new FragmentRules();

	public enum ACCESS{
		DIRECT,DUPLICATE, EQUIVALENT, ND2
	}
	
	ArrayList<Fragment> fragments = new ArrayList<>();
	
	List<Set<StateVertex>> nearDuplicates = new ArrayList<Set<StateVertex>>();
	
	HashMap<StatePair, StatePair> stateComparisionCache = new HashMap<>();
	
	private Provider<InMemoryStateFlowGraph> sfg;
	
	HashMap<Integer, Double> hops = new HashMap<>();
	private HashMap<Integer, Double> numNonSelections = new HashMap<>();

	public FragmentManager(Provider<InMemoryStateFlowGraph> graphProvider) {
		this.sfg = graphProvider;
	}
	

	public Set<StatePair> getStateComparisionCache() {
		return this.stateComparisionCache.keySet();
	}
	
	public List<Set<StateVertex>> getNearDuplicates(){
		return this.nearDuplicates;
	}
	
	public StatePair getCachedComparision(StateVertex state1, StateVertex state2) {
		StatePair temp = new StatePair(state1, state2, null, null, null);
		try {
			return stateComparisionCache.get(temp);
		}catch(Exception ex) {
			return null;
//			ex.printStackTrace();
		}
//		if(stateComparisionCache.containsKey(temp)){
//			return stateComparisionCache.get(temp);
//		}
//		return null;
	}
	
	public boolean cacheStateComparision(StatePair statePair, boolean assignDynamic) {
		if(!stateComparisionCache.containsKey(statePair)){
			stateComparisionCache.put(statePair, statePair);
			StateComparision comp = statePair.getStateComparision();
			if(assignDynamic && (comp.equals(StateComparision.NEARDUPLICATE1) || comp.equals(StateComparision.NEARDUPLICATE2))) {
				List<Fragment> dynamic = ((HybridStateVertexImpl)statePair.getState1()).assignDynamicFragments(statePair.getState2());
				for(Fragment dyn : dynamic) {
					setDynamic(dyn);
				}
			}
		}
		else {
			return false;
		}
		return true;
	}
	
	private void setDynamic(Fragment dyn) {
		List<Fragment> related = getRelatedFragments(dyn);
		for(Fragment rel: related) {
			rel.setDynamic(true);
		}
	}
	
	public boolean setCoverage(Fragment newFragment) {
		if(newFragment.isAccessTransferred())
			// Already access transferred
			return true;
		
		if(newFragment.isGlobal()) {
			if(!newFragment.getEquivalentFragments().isEmpty()) {
				for(Fragment oldFragment: newFragment.getEquivalentFragments()) {
					if(oldFragment.equals(newFragment))
						continue;
					newFragment.transferCoverage(oldFragment);
					LOG.info("equivalent coverage transfer {} {} ", oldFragment.getId(), oldFragment.getReferenceState().getName());
				}
			}
		}
		else {
			if(!newFragment.getDuplicateFragments().isEmpty()) {
				for(Fragment oldFragment: getDuplicateFragments(newFragment)) {
					if(oldFragment.equals(newFragment))
						continue;
					newFragment.transferCoverage(oldFragment);
					LOG.info("duplicate coverage transfer {} {} ", oldFragment.getId(), oldFragment.getReferenceState().getName());
				}
				
			}
		}
		return true;
	}


	public boolean setAccess(Fragment newFragment) {
		LOG.info("Setting access for {} {}", newFragment.getId(), newFragment.getReferenceState().getName());
		if(newFragment.isGlobal()) {
			if(!newFragment.getEquivalentFragments().isEmpty()) {
				for(Fragment oldFragment: newFragment.getEquivalentFragments()) {
					newFragment.transferEquivalentAccess(oldFragment);
					LOG.info("equivalent transfer access {} {} ", oldFragment.getId(), oldFragment.getReferenceState().getName());
				}
			}
		}
		else {
			if(!newFragment.getDuplicateFragments().isEmpty()) {
				for(Fragment oldFragment: getDuplicateFragments(newFragment)) {
					newFragment.transferDuplicateAccess(oldFragment);
					LOG.info("duplicate transfer access {} {} ", oldFragment.getId(), oldFragment.getReferenceState().getName());
				}
				for(Fragment oldFragment: getEquivalentFragments(newFragment)) {
					newFragment.transferEquivalentAccess(oldFragment);
					LOG.info("equivalent transfer access {} {} ", oldFragment.getId(), oldFragment.getReferenceState().getName());
				}
			}
		}
		
		if(!newFragment.getNd2Fragments().isEmpty()) {
			for(Fragment oldFragment: newFragment.getNd2Fragments()) {
				newFragment.transferEquivalentAccess(oldFragment);
				LOG.info("nd2 transfer access {} {} ", oldFragment.getId(), oldFragment.getReferenceState().getName());
			}
		}
		return true;
	}
	
	public boolean addFragment(Fragment fragment, boolean fast) {
		ArrayList<Fragment> equivalentFragments = new ArrayList<Fragment>();
		ArrayList<Fragment> nd2Fragments = new ArrayList<Fragment>();
		for (Fragment existingFragment : fragments) {
			try {
				
				FragmentComparision comp = fast? existingFragment.compareFast(fragment) : existingFragment.compare(fragment);
				switch (comp) {
				case EQUAL:
					existingFragment.addDuplicateFragment(fragment);
					fragment.addDuplicateFragment(existingFragment);
					fragment.setIsGlobal(false);
					return false;
				case EQUIVALENT:
					equivalentFragments.add(existingFragment);
					break;
				case ND2:
					nd2Fragments.add(existingFragment);
					break;
				case DIFFERENT:
				default:
				}
			}catch(Exception ex) {
				LOG.error("Error comparing Fragments {} in  {} and {} in {}", fragment.getId(), fragment.getReferenceState(), existingFragment.getId(), existingFragment.getReferenceState());
				ex.printStackTrace();
			}
		}
		
		fragment.setIsGlobal(true);
		fragments.add(fragment);

		for (Fragment existingFragment : equivalentFragments) {
			existingFragment.addEquivalentFragment(fragment);
			fragment.addEquivalentFragment(existingFragment);
		}
		
		for(Fragment existingFragment: nd2Fragments) {
			existingFragment.addND2Fragment(fragment);
			fragment.addND2Fragment(existingFragment);
		}
		
		if(fragment.getReferenceState().getCandidateElements()!=null && usefulFragment(fragment)) {
			setAccess(fragment);
			setCoverage(fragment);
			LOG.info("Access Transferred");
			fragment.setAccessTransferred(true);
		}
		
		LOG.debug("Added framgnet {}", XPathHelper.getXPathExpression(fragment.getFragmentParentNode()));

		return true;
	}
	
	public boolean updateInfluence(Fragment fragment, ACCESS access) {
		if(fragment.getParent() !=null)
			updateInfluence(fragment.getParent(), access);
		
		if(fragment.getCandidateInfluence()==null) {
			return false;
		}
		
		double influence = fragment.getCandidateInfluence();
		
		switch(access) {
		case DIRECT:
			fragment.setCandidateInfluence(influence - 1.0);
			break;
		case DUPLICATE:
			fragment.setCandidateInfluence(influence - 0.5);
			break;
		case EQUIVALENT:
			fragment.setCandidateInfluence(influence - 0.25);
			break;
		case ND2:
			break;
		default:
			break;
		}
		return true;
	}
	
	
	public boolean recordCoverage(Node node, StateVertex state, Coverage coverage) {
		Fragment closest = state.getClosestFragment(node);
		if(closest == null) {
			LOG.error("Could not find closest fragment for given node {}", node);
			return false;
		}
		closest.setCoverage(node, AccessType.direct, coverage);
		for (Fragment related: getRelatedFragments(closest)){
			if (related == closest || !usefulFragment(related))
				continue;
			related.setEquivalentCoverage(node, closest, AccessType.equivalent, coverage);
//			(node, AccessType.equivalent, coverage);
		}
		return true;
	}

	public boolean recordAccess(CandidateElement element, StateVertex state) {
		if(state.getRootFragment()!=null && !state.getRootFragment().isAccessTransferred()) {
			setAccess(state);
		}
		
		state.setDirectAccess(element);
//		element.setDirectAccess(true);

		ArrayList<CandidateElement> coveredCandidates = new ArrayList<CandidateElement>();
		Fragment closestFragment = null;
		try {
			closestFragment = state.getClosestFragment(element);
		} catch (Exception e) {
			LOG.error("Error getting closest Fragment for {}", element);
		}
		

		if (closestFragment == null) {
			LOG.error("No fragment contains the candidate element");
			return false;
		}
		
		LOG.info("direct access for closest fragment {} in {} {}", closestFragment.getId(), state.getName(), XPathHelper.getXPathExpression(element.getElement()));
		
		updateInfluence(closestFragment, ACCESS.DIRECT);

		recordDuplicateAccess(element, coveredCandidates, closestFragment);
		
//		coveredCandidates = new ArrayList<CandidateElement>();
		recordEquivalentAccess(element, coveredCandidates, closestFragment);
		
		boolean domAccessNeed = false;
		
		try {
			domAccessNeed = closestFragment.getFragmentParentNode() == null || state.getClosestDomFragment(element).getId()!= state.getClosestFragment(element).getId();
		}catch(Exception ex) {
			LOG.error("Error getting closest Fragment for {}", element);
		}
		
		if(domAccessNeed) {
			LOG.info("Recording access DOM fragments in {} {}", state.getName(), XPathHelper.getXPathExpression(element.getElement()));
			closestFragment = state.getClosestDomFragment(element);
			recordDuplicateAccess(element, coveredCandidates, closestFragment);
			recordEquivalentAccess(element, coveredCandidates, closestFragment);
		}
		
		recordNearDuplicateAccess(element, state);

//		for (CandidateElement candidate : coveredCandidates) {
//			if (candidate == element)
//				continue;
//
//			candidate.incrementAccess();
//		}

		return true;
	}


	private void recordNearDuplicateAccess(CandidateElement element, StateVertex state) {
		ArrayList<CandidateElement> coveredCandidates;
		if(state.hasNearDuplicate() && state.getRootFragment()==null) {
			LOG.info("Ignored a near-duplicate with no root {}", state.getName());
		}
		
		if(state.hasNearDuplicate() && state.getRootFragment()!=null) {
			// Record equivalent access for near-duplicate states
			coveredCandidates = new ArrayList<CandidateElement>();

			Set<StateVertex> nearDuplicateStates = getNearDuplicates(state);
			for(StateVertex nearDuplicate : nearDuplicateStates) {
				if(nearDuplicate.getId() == state.getId())
					continue;
				if(nearDuplicate.getRootFragment()==null)
					continue;
				
				Fragment equivalentFragment = nearDuplicate.getRootFragment();
				List<CandidateElement> equivalentCandidates = equivalentFragment.recordEquivalentCandidateAccess(element,
						state.getRootFragment(), coveredCandidates);
				if (equivalentCandidates != null && !equivalentCandidates.isEmpty()) {
					for(CandidateElement equivalentCandidate: equivalentCandidates) {
						if (!coveredCandidates.contains(equivalentCandidate)) {
							coveredCandidates.add(equivalentCandidate);
							updateInfluence(equivalentFragment, ACCESS.EQUIVALENT);
							LOG.info("nd access {} {}", equivalentFragment.getId(), equivalentFragment.getReferenceState().getName());
						} else {
							LOG.warn("Something Wrong : candidate reappearing : " + equivalentCandidate);
						}
					}
				}else {
					LOG.warn("Could not record equivalent access for {} in {}", element.getIdentification(), equivalentFragment.getReferenceState().getName());
				}
			}
		}
	}


	private void recordEquivalentAccess(CandidateElement element, ArrayList<CandidateElement> coveredCandidates,
			Fragment closestFragment) {
		ArrayList<Fragment> equivalentFragments = getEquivalentFragments(closestFragment);
		for (Fragment equivalentFragment : equivalentFragments) {
			if (equivalentFragment == closestFragment || !usefulFragment(equivalentFragment))
				continue;

			List<CandidateElement> equivalentCandidates = equivalentFragment.recordEquivalentCandidateAccess(element,
					closestFragment, coveredCandidates);
			if (equivalentCandidates != null && !equivalentCandidates.isEmpty()) {
				if (!coveredCandidates.containsAll(equivalentCandidates)) {
					for(CandidateElement equivalentCandidate: equivalentCandidates) {
						if(!coveredCandidates.contains(equivalentCandidate)) {
							coveredCandidates.add(equivalentCandidate);
							updateInfluence(equivalentFragment, ACCESS.EQUIVALENT);
							LOG.info("equivalent access {} {}", equivalentFragment.getId(), equivalentFragment.getReferenceState().getName());
						}
					}
				} else {
					LOG.warn("Something Wrong : candidate reappearing : " + equivalentCandidates);
				}
			}else {
				LOG.warn("Could not record equivalent access for {} in {}", element.getIdentification(), equivalentFragment.getReferenceState().getName());
			}

		}
	}


	private void recordDuplicateAccess(CandidateElement element, ArrayList<CandidateElement> coveredCandidates,
			Fragment closestFragment) {
		ArrayList<Fragment> duplicateFragments = getDuplicateFragments(closestFragment);
		for (Fragment duplicateFragment : duplicateFragments) {
			if (duplicateFragment == closestFragment)
				continue;

			List<CandidateElement> duplicateCandidates = duplicateFragment.recordDuplicateCandidateAccess(element,
					closestFragment, coveredCandidates);

			if (duplicateCandidates != null && !duplicateCandidates.isEmpty()) {
				for(CandidateElement duplicateCandidate: duplicateCandidates) {
					if (!coveredCandidates.contains(duplicateCandidate)) {
						coveredCandidates.add(duplicateCandidate);
						updateInfluence(duplicateFragment, ACCESS.DUPLICATE);
						LOG.info("duplicate access {} {}", duplicateFragment.getId(), duplicateFragment.getReferenceState().getName());
					} else {
						LOG.error("Something Wrong : candidate reappearing : " + duplicateCandidate);
					}
				}
			}else {
				
				LOG.warn("Could not record duplicate access : " + element.getIdentification());
			}
		}
	}
	
	private Set<StateVertex> getNearDuplicates(StateVertex state) {
		Set<StateVertex> returnSet = new HashSet<StateVertex>();
		for(Set<StateVertex> nearduplicateSet: nearDuplicates) {
			if(nearduplicateSet.contains(state)) {
				returnSet.addAll(nearduplicateSet);
			}
		}
		
		return returnSet;
	}


	public void setAccess(StateVertex state) {
		for(Fragment fragment : state.getFragments()) {
			if(!usefulFragment(fragment))
				continue;
			setAccess(fragment);
			setCoverage(fragment);
			fragment.setAccessTransferred(true);
		}
		LOG.info("Access transferred for {}", state.getName());
	}


	
	private static int getFragmentWidth(Fragment fragment) throws XPathExpressionException{
		if(fragment.getFragmentParentNode() == null) {
			int subtreeWidth = 0;
			for(Node nestedBlock : fragment.getNestedBlocks()) {
				if(!nestedBlock.getNodeName().equalsIgnoreCase("#text")) {
					int blockWidth = DomUtils.getNumLeafNodes(nestedBlock);
					if(blockWidth == 0)
						blockWidth = 1;
					subtreeWidth = subtreeWidth + blockWidth;
				}
			}
			return subtreeWidth;
		}
		return DomUtils.getNumLeafNodes(fragment.getFragmentParentNode());
	}

	/**
	 * To decide if the fragment is big enough to be used for record equivalent
	 * accesses
	 * 
	 * @param fragment
	 * @return
	 */
	public static boolean usefulFragment(Fragment fragment) {
		try {
			if(fragment.isUseful() !=null) {
				return fragment.isUseful();
			}
			int subtreeWidth = getFragmentWidth(fragment);
			if (
					(fragment.getRect().getWidth() > USEFUL_FRAGMENT_RULES.getThresholdWidth() &&
							fragment.getRect().getHeight() > USEFUL_FRAGMENT_RULES.getThresholdHeight() &&
							subtreeWidth >= USEFUL_FRAGMENT_RULES.getSubtreeWidth_and()) 
					|| subtreeWidth >= USEFUL_FRAGMENT_RULES.getSubtreeWidth_or()) {
				fragment.setUseful(true);
				return true;
				}
		} catch (XPathExpressionException e) {
			LOG.error("XPATH Error checking if the fragment is useful");
		} catch(Exception ex) {
			LOG.error(" Error checking if the fragment is useful");
			LOG.error(ex.getMessage());
		}
		fragment.setUseful(false);
		return false;
	}

	public ArrayList<Fragment> getDuplicateFragments(Fragment fragment) {
		ArrayList<Fragment> duplicateFragments = new ArrayList<Fragment>();
		if(fragment == null) {
			return duplicateFragments;
		}
		Fragment globalFragment = null;
		if (fragment.isGlobal()) {
			globalFragment = fragment;
		} else {
			if(fragment.getDuplicateFragments().isEmpty()) {
				LOG.error("Fragment neither global nor connected to global : " + fragment + "in " + fragment.getReferenceState().getName());
				duplicateFragments.add(fragment);
				return duplicateFragments;
			}
			
			globalFragment = fragment.getDuplicateFragments().get(0);
		}

		duplicateFragments.add(globalFragment);
		duplicateFragments.addAll(globalFragment.getDuplicateFragments());

		return duplicateFragments;
	}

	public ArrayList<Fragment> getEquivalentFragments(Fragment fragment) {
		ArrayList<Fragment> equivalentFragments = new ArrayList<Fragment>();
		if(fragment==null) {
			return equivalentFragments;
		}
		if (!fragment.isGlobal()) {
			Fragment globalFragment = fragment.getDuplicateFragments().get(0);
			equivalentFragments = getEquivalentFragments(globalFragment);
		} else {
			equivalentFragments = fragment.getEquivalentFragments();
		}
		
		Set<Fragment> dupOfEquivalent = new HashSet<Fragment>();
		dupOfEquivalent.addAll(equivalentFragments);
		for(Fragment equivalent: equivalentFragments) {
			dupOfEquivalent.addAll(equivalent.getDuplicateFragments());
		}
		equivalentFragments.clear();
		equivalentFragments.addAll(dupOfEquivalent);
		return equivalentFragments;
	}

	public ArrayList<Fragment> getRelatedFragments(Fragment fragment) {
		ArrayList<Fragment> relatedFragments = new ArrayList<Fragment>();
		relatedFragments.addAll(getDuplicateFragments(fragment));
		relatedFragments.addAll(getEquivalentFragments(fragment));
		return relatedFragments;
	}
	
	
	public StateVertex getClosestUnexploredState(StateVertex currentState,  List<StateVertex> onURLSet, BlockingQueue<Integer> statesWithCandidates, boolean applyNonSelAdvantage) {
		// Selector = Influence - Hops
		long start = System.currentTimeMillis();
		HashMap<StateVertex, Double> hops = new HashMap<>();

		double maxInfluence = -1000000;
		StateVertex maxState = null;
		
		
		boolean unexploredStateFound = false;
		
		boolean unexploredNearDuplicateFound = false;
		
		for(StateVertex state: sfg.get().getAllStates()) {
			if(!statesWithCandidates.contains(state.getId())) {
				continue;
			}
			
			
			
			if(!unexploredStateFound && state.hasUnexploredActions()) {
				LOG.info("Unexplored State {} found ", state.getName());
				
				unexploredStateFound = true;
				maxInfluence = -1000000;
				maxState = null;
			}
			
			if(unexploredStateFound && !unexploredNearDuplicateFound && !hasExploredNearDuplicate(state)) {
				unexploredNearDuplicateFound = true;
				maxInfluence = -1000000;
				maxState = null;
			}
			
			if(unexploredStateFound && !state.hasUnexploredActions()) {
				continue;
			}
			
			if(unexploredNearDuplicateFound && hasExploredNearDuplicate(state)) {
				// Skipping unexplored states that have an explored nearduplicate
				continue;
			}
			
			try{
				double influence = 0;
				double candidateInfluence = calculateFragmentCandidateInfluence(state.getRootFragment());
				double hopInfluence = calculateHops(state.getRootFragment(), currentState, onURLSet);
				// Remove this assigning non seelctions: already done in unfiredCandidates.seenState()
				if(numNonSelections.containsKey(state.getId())) {
//					double current = numNonSelections.get(state.getId());
//					numNonSelections.replace(state.getId(), current+1);
				}
				else {
					numNonSelections.put(state.getId(), 1.0);
				}
				double nonSelectionAdvantage = numNonSelections.get(state.getId());
				
				influence = candidateInfluence - hopInfluence;
				if(applyNonSelAdvantage) {
					 influence += nonSelectionAdvantage;
					LOG.info("{} : candidate {}, hop{}, nonSel {}, influence {}", state.getName(), candidateInfluence, hopInfluence, nonSelectionAdvantage, influence);
				}
				else {
					LOG.info("{} : candidate {}, hop{}, influence {}", state.getName(), candidateInfluence, hopInfluence, influence);
				}
				
				if(influence > maxInfluence) {
					maxInfluence = influence ;
					maxState = state;
				}
			}catch(Exception ex) {
//				ex.printStackTrace();
				LOG.error("Error Calculating influence of state : " + state.getName());
			}
		}
		
		long end = System.currentTimeMillis();
		LOG.info("Time taken to find next best state : "  + (end-start) + " millis");
		
//		numNonSelections.replace(maxState.getId(), 0.0);
		return maxState;
	}

	public StateVertex getClosestUnexploredFragment(StateVertex currentState, List<StateVertex> onURLSet, BlockingQueue<Integer> statesWithCandidates) {
		// Selector = Influence - Hops
		long start = System.currentTimeMillis();
		HashMap<StateVertex, Double> hops = new HashMap<>();

		double maxInfluence = -100;
		Fragment maxFragment = null;
		try {
			for (Fragment fragment : fragments) {
				
				if(fragment == null) {
					continue;
				}
				if(fragment.getCandidates().isEmpty()) {
					continue;
				}
				
				if(!statesWithCandidates.contains(fragment.getReferenceState().getId())){
					continue;
				}
				
				int duplication = fragment.getDuplicateFragments().size();
				int equivalence = fragment.getEquivalentFragments().size();
				
				int duplicationFactor = 2*duplication + equivalence;
	
				double fragmentCandidateInfluence = calculateFragmentCandidateInfluence(fragment);
				double hopInfluence = 0.0;
				if(hops.containsKey(fragment.getReferenceState()))
					hopInfluence = hops.get(fragment.getReferenceState());
				else {
					hopInfluence = calculateHops(fragment, currentState, onURLSet);
					hops.put(fragment.getReferenceState(), hopInfluence);
				}
				
				double influence = fragmentCandidateInfluence*duplicationFactor - hopInfluence;
				if(influence > maxInfluence) {
					maxInfluence = influence;
					maxFragment = fragment;
				}
				
				ArrayList<Fragment> duplicates = fragment.getDuplicateFragments();
				for (Fragment duplicate : duplicates) {
					double fragmentCandidateInfluence2 = calculateFragmentCandidateInfluence(duplicate);
					double influence2 = fragmentCandidateInfluence2 - calculateHops(duplicate, currentState, onURLSet);
					if(influence2 > maxInfluence) {
						maxInfluence = influence2;
						maxFragment = duplicate;
					}
				}
			}
		}
		catch(Exception ex) {
			LOG.error("Error getting the closest unexplored fragment");
			LOG.error(ex.getMessage());
//			ex.printStackTrace();
		}
		finally {
			long end = System.currentTimeMillis();
			LOG.info("Time taken to Find the best FRAGMENT : " + (end-start) + " millis");
		}
		

		return maxFragment==null? null : maxFragment.getReferenceState();
		
	}

	private double calculateHops(Fragment fragment, StateVertex currentState, List<StateVertex> onURLSet) {
		
		if(fragment.getReferenceState().getId()  == currentState.getId()) {
			return 0;
		}
		
		if(hops.containsKey(currentState.getId())){
			return hops.get(currentState.getId());
		}
		
		double averageHopsFromURLLoad = 0;
		int size = onURLSet.size();
		
		for(StateVertex onURL: onURLSet) {
//			if(sfg.get().canGoTo(onURL, fragment.getReferenceState())) {
			try {
				averageHopsFromURLLoad += sfg.get().getShortestPath(onURL, fragment.getReferenceState()).size();
			}catch(Exception ex) {
				size = size-1;
			}
				
//			}
		}
		
		if(size <= 0) {
			LOG.info("This state seems unreachable!!");
			return -1;
		}
		
		// 1 Hop to load the URL 
		averageHopsFromURLLoad = averageHopsFromURLLoad/size + 1;
		
//		if(sfg.get().canGoTo(currentState, fragment.getReferenceState())) {
//		try {
//			if(sfg.get().getOutgoingStates(currentState).size() != 0) {
//				double hopsFromCurrent = sfg.get().getShortestPath(currentState, fragment.getReferenceState()).size();
//				return Math.min(hopsFromCurrent, averageHopsFromURLLoad);
//			}
//		}catch(Exception ex) {
//			LOG.info("Unreachable from current state");
//		}
		
		
		if(!hops.containsKey(currentState.getId())){
			hops.put(currentState.getId(), averageHopsFromURLLoad);
		}
		return averageHopsFromURLLoad;
	}
	
	public double calculateCandidateInfluence(CandidateElement candidate) {
		
		double candidateInfluence = 1.0;
		if (candidate.isDirectAccess()) {
			candidateInfluence = 0.0;
		}
		
		candidateInfluence = candidateInfluence - (0.5 * candidate.getDuplicateAccess() + 0.25* candidate.getEquivalentAccess());
		
		return candidateInfluence;
	}

	private double calculateFragmentCandidateInfluence(Fragment fragment) {
		if(fragment.getCandidateInfluence()!=null) {
			return fragment.getCandidateInfluence();
		}
		
		double fragmentInfluence = 0.0;
		
		if(!fragment.getChildren().isEmpty()) {
			for(Fragment child: fragment.getChildren()) {
				fragmentInfluence = fragmentInfluence + calculateFragmentCandidateInfluence(child);
			}
		}
		
		ArrayList<CandidateElement> candidates = fragment.getCandidates();
		for (CandidateElement candidate : candidates) {
			if(candidate.getClosestFragment().equals(fragment)) {
				double candidateInfluence = calculateCandidateInfluence(candidate);
				fragmentInfluence += candidateInfluence;
			}
		}
		
		fragment.setCandidateInfluence(fragmentInfluence);
		return fragmentInfluence;
	}

	public double calculateDuplicationFactor(CandidateElement element, StateVertex state) {
		Fragment closest = null;
		try {
			closest = state.getClosestFragment(element);
		} catch (Exception e) {
			LOG.error("Error getting Closest fragment for {} in {}", element, state);
			LOG.error(e.getMessage());
		}
		
		if(closest == null) {
//			LOG.error("No Close fragment?" + element + " in : " + state.getName());
			return 1;
		}
		double factor = 0;
		factor += 2*getDuplicateFragments(closest).size() - 1;
		factor += getEquivalentFragments(closest).size();
		
		return factor;
	}

	
	public static List<Fragment> getLeafFragments(List<Fragment> fragments){
		List<Fragment> returnFrags = new ArrayList<>();
		for(Fragment fragment : fragments) {
			if(fragment.getChildren().isEmpty()) {
				if(usefulFragment(fragment)) {
					returnFrags.add(fragment);
				}
				else {
					boolean addedParent = false;
					Fragment parent = fragment.getParent();
					while(parent !=null) {
						if(usefulFragment(parent)) {
							if(!returnFrags.contains(parent))
								returnFrags.add(parent);
							addedParent = true;
							break;
						}
						parent = parent.getParent();
					}
					if(addedParent) {
						LOG.debug("Added parent{} to the leaf node because child {} is not useful", parent.getId(), fragment.getId());
					}
				}
			}
		}
		return returnFrags;
	}
	
	public HashMap<Integer, Integer> getLeafFragmentMapping(StateVertex state1, StateVertex state2){
		List<Fragment> newFragments = getLeafFragments(state1.getFragments());
		
		List<Fragment> expectedFragments = getLeafFragments(state2.getFragments());
		
		HashMap<Integer, Integer> mapping = new HashMap<>();
		
		boolean uniqueMap = true;
		boolean allMappingFound = true;
		
		for(Fragment newFragment : newFragments) {
			boolean mappingFound = false;
			boolean unique = true;
			for(Fragment expected: expectedFragments) {
				if(getRelatedFragments(newFragment).contains(expected)) {
					if(mapping.containsValue(expected.getId())) {
						LOG.debug("Repeat of mapping");
						mappingFound = true;
						unique = false;
						mapping.put(newFragment.getId(), expected.getId());
					}
					else {
						if(!unique) {
							unique = true;
							mapping.remove(newFragment.getId());
						}
						mappingFound = true;
						LOG.debug("mapped : " + newFragment.getId() + " " + expected.getId());
						mapping.put(newFragment.getId(), expected.getId());
						break;
					}
				}
			}
			if(!mappingFound) {
				mapping.put(newFragment.getId(), -1);
			}
			allMappingFound = mappingFound && allMappingFound;
			uniqueMap = unique && uniqueMap;
		}
		
		int negative = -1;
		for(Fragment expected: expectedFragments) {
			if(!mapping.containsValue(expected.getId())) {
				mapping.put(negative, expected.getId());
				negative-=1;
			}
		}
		
		return mapping;
	}
	
	public StateComparision areND2(StateVertex newState, StateVertex expectedState) {
		List<Fragment> newFragments = getLeafFragments(newState.getFragments());
		
		List<Fragment> expectedFragments = getLeafFragments(expectedState.getFragments());
		
		if (newFragments.size() == expectedFragments.size()) {
			LOG.debug("May be near duplicates of type 1");
		}
		
		HashMap<Integer, Integer> mapping = new HashMap<>();
		
		int newF = 0;
		int oldF = 0;
		
		boolean uniqueMap = true;
		boolean allMappingFound = true;
		
		for(Fragment newFragment : newFragments) {
			LOG.info("new" + newFragment.getId());
			boolean mappingFound = false;
			boolean unique = true;
			for(Fragment expected: expectedFragments) {
				FragmentComparision comp = expected.compare(newFragment);
				LOG.debug(" old " + expected.getId());
				LOG.debug("compared " + comp);
//				List<Fragment> related = getRelatedFragments(newFragment);
//				List<Fragment> related2 = getRelatedFragments(expected);
				if(getRelatedFragments(newFragment).contains(expected)) {
					if(mapping.containsValue(oldF)) {
						LOG.debug("Repeat of mapping");
						mappingFound = true;
						unique = false;
						mapping.put(newF, oldF);
					}
					else {
						if(!unique) {
							unique = true;
							mapping.remove(newF);
						}
						mappingFound = true;
						LOG.debug("mapped : " + newFragment.getId() + " " + expected.getId());
						mapping.put(newF, oldF);
						break;
					}
				}
				oldF+=1;
			}
			if(!mappingFound) {
				LOG.info("No mapping found for {}", newFragment.getId());
			}
			allMappingFound = mappingFound && allMappingFound;
			uniqueMap = unique && uniqueMap;
			newF +=1;
			oldF=0;
		}
		
		if(!allMappingFound)
			return StateComparision.DIFFERENT;
		
		if(!uniqueMap)
			return StateComparision.NEARDUPLICATE2;
		else
			return StateComparision.NEARDUPLICATE1;

	}
	
	/**
	 * Color histogram comparison
	 * @param image1
	 * @param image2
	 * @return
	 */
	public boolean compareImages(BufferedImage image1, BufferedImage image2) {
		ColorHistogram colorHistogram = new ColorHistogram();
		Mat chist1 = colorHistogram.getHistogram(image1);
		Mat chist2 = colorHistogram.getHistogram(image2);
		double comp = ColorHistogram.compare(chist1, chist2);
		
		if(comp==0)
			return true;
		return false;
	}
	
	
	
	public StateComparision cacheStateComparision(StateVertex newState, StateVertex expectedState, boolean assignDynamic) {
	LOG.debug(" ");
		if(getCachedComparision(newState, expectedState)==null) {
			
			StateComparision comp = null;
			
			if(newState.equals(expectedState)) {
				LOG.info("{} has same structure as expected {}", newState.getName(), expectedState.getName());
				boolean screenshotSame = false;
				if((newState instanceof HybridStateVertexImpl) && (expectedState instanceof HybridStateVertexImpl)) {
					if(((HybridStateVertexImpl)newState).getImage()!=null && ((HybridStateVertexImpl)expectedState).getImage()!=null ) {
						screenshotSame = compareImages(((HybridStateVertexImpl)newState).getImage(), ((HybridStateVertexImpl)expectedState).getImage());
					}
					if(screenshotSame) {
						comp = StateComparision.DUPLICATE;
					}
					else {
						comp = StateComparision.NEARDUPLICATE1;
					}
					addToNearDuplicates(newState, expectedState);
					StatePair statePair = new StatePair(newState, expectedState, null, null, comp);
					cacheStateComparision(statePair, assignDynamic);
					return comp;
				}
			}
			
			else if(newState.getFragments() == null || expectedState.getFragments() == null) {
				addToNearDuplicates(newState);
				addToNearDuplicates(expectedState);
				LOG.warn("{} and {} could not be compared",newState, expectedState );
				comp = StateComparision.ERRORCOMPARING;
				StatePair statePair = new StatePair(newState, expectedState, null, null, comp);
				cacheStateComparision(statePair, assignDynamic);
				
				return comp;
			}
			
			else if(getRelatedFragments(newState.getRootFragment()).contains(expectedState.getRootFragment())) {
				LOG.info("The root fragments of two states are either equivalent or duplicates");
				comp = StateComparision.NEARDUPLICATE1;
				addToNearDuplicates(newState, expectedState);
				StatePair statePair = new StatePair(newState, expectedState, null, null, comp);
				cacheStateComparision(statePair, assignDynamic);
				return comp;
			}
			
			/**
			 * Compare with differing nodes.
			 */
			List<List<Node>> differentNodes = ((HybridStateVertexImpl)newState).getDifference(expectedState);
			if(differentNodes == null) {
				addToNearDuplicates(newState);
				addToNearDuplicates(expectedState);
				LOG.warn("{} and {} could not be compared",newState, expectedState );
				comp = StateComparision.ERRORCOMPARING;
				StatePair statePair = new StatePair(newState, expectedState, null, null, comp);
				cacheStateComparision(statePair, assignDynamic);
				
				return comp;
			}
			List<Node> addedNodes = differentNodes.get(0);
			List<Node> removedNodes = differentNodes.get(1);
			
			List<Fragment> affectedNewFragments = getAffectedFragments(addedNodes, newState);
			List<Fragment> affectedOldFragments = getAffectedFragments(removedNodes, expectedState);
			
			if(affectedNewFragments == null || affectedOldFragments==null) {
				addToNearDuplicates(newState);
				addToNearDuplicates(expectedState);
				LOG.warn("{} and {} could not be compared",newState, expectedState );
				comp = StateComparision.ERRORCOMPARING;
				StatePair statePair = new StatePair(newState, expectedState, null, null, comp);
				cacheStateComparision(statePair, assignDynamic);
				
				return comp;
			}
			
			boolean allCovered = true;
			for(Fragment affectedFragment: affectedNewFragments) {
				List<Fragment> relatedFragments = getRelatedFragments(affectedFragment);
				boolean covered = false;
				for(Fragment relatedFragment: relatedFragments) {
					if(relatedFragment.getReferenceState().getId() == expectedState.getId()){
						LOG.debug("Mapped affected Fragment {} {} to {} {}", newState.getName(), affectedFragment.getId(),
								expectedState.getName(), relatedFragment.getId());
						covered = true;
						break;
					}
				}
				allCovered = allCovered && covered;
			}
			

//			boolean allCovered = true;
			for(Fragment affectedFragment: affectedOldFragments) {
				List<Fragment> relatedFragments = getRelatedFragments(affectedFragment);
				boolean covered = false;
				for(Fragment relatedFragment: relatedFragments) {
					if(relatedFragment.getReferenceState().getId() == newState.getId()){
						LOG.debug("Mapped affected Fragment {} {} to {} {}", expectedState.getName(), affectedFragment.getId(),
								newState.getName(), relatedFragment.getId());
						covered = true;
						break;
					}
				}
				allCovered = allCovered && covered;
			}
			
//			if(newState.getRootFragment().isND2Fragment(expectedState.getRootFragment(), this)) {
			if(allCovered) {
				LOG.info("NEARDUPLICATE2 {} , {}" , newState.getName(), expectedState.getName() );
				comp = StateComparision.NEARDUPLICATE2;
				addToNearDuplicates(newState, expectedState);
			}
			else {
				comp = StateComparision.DIFFERENT;
				addToNearDuplicates(newState);
				addToNearDuplicates(expectedState);
			}
			
//
//			LOG.debug("**************************************************************");
//
//			LOG.debug("*************************   ADDED   ***************************************");
//			LOG.debug(newState.getName());
//			for(Node node: addedNodes) {
//				if(node.getNodeName().equalsIgnoreCase("#text"))
//					continue;
//				LOG.debug("Added           :     "+  XPathHelper.getSkeletonXpath(node));
//				try {
//					Fragment closestFragment= getClosestFragment(node, newState);
//					LOG.debug("Affected Fragment : " + closestFragment.getId() );
//				}catch(Exception ex) {
//					ex.printStackTrace();
//				}
//				// Set the fragment closest to the node as dynamic ? 
//			}
//			LOG.debug("*************************  REMOVED  *************************************");
//
//			LOG.debug(expectedState.getName());
//			for(Node node: removedNodes) {
//				if(node.getNodeName().equalsIgnoreCase("#text"))
//					continue;
//				LOG.debug("Removed         :      "+  XPathHelper.getSkeletonXpath(node));
//				// Set the fragment closest to the node as dynamic ? 
//				try {
//					Fragment closestFragment= getClosestFragment(node, expectedState);
//					LOG.debug("Affected Fragment : " + closestFragment.getId() );
//				}catch(Exception ex) {
//					ex.printStackTrace();
//				}
//			}
////					LOG.debug(removedNodes);
//			LOG.debug("**************************************************************");

			
			
			StatePair statePair = new StatePair(newState, expectedState, addedNodes, removedNodes, comp);
			cacheStateComparision(statePair, assignDynamic);
			return comp;
		}
		else {
			LOG.debug("Already Compared : " + getCachedComparision(newState, expectedState));
			return getCachedComparision(newState, expectedState).getStateComparision();
		}
	}


	private List<Fragment> getAffectedFragments(List<Node> changedNodes, StateVertex state) {

		List<Fragment> affectedFragments = new ArrayList<Fragment>();
		
		Node lca =  VipsUtils.getParentBox(changedNodes);
//		LOG.debug("Lca : " + XPathHelper.getSkeletonXpath(lca));
		if(changedNodes.contains(lca)) {
			LOG.debug("All Changed Nodes are a single subtree {}",  XPathHelper.getSkeletonXpath(lca));
			Fragment closestFragment= state.getClosestFragment(lca);
			if(closestFragment!=null) {
				if(!affectedFragments.contains(closestFragment))
					affectedFragments.add(closestFragment);
				LOG.debug("Affected Fragment in {} is {}" , state.getName(),  closestFragment.getId() );
			}
			return affectedFragments;
		}
		
		
//		LOG.debug("State : " + state.getName());
		for(Node node: changedNodes) {
			if(node.getNodeName().equalsIgnoreCase("#text"))
				continue;
			LOG.debug("Node :     "+  XPathHelper.getSkeletonXpath(node));
			try {
				Fragment closestFragment= state.getClosestFragment(node);
				if(closestFragment!=null) {
					if(!affectedFragments.contains(closestFragment)) {
						affectedFragments.add(closestFragment);
						LOG.debug(XPathHelper.getSkeletonXpath(node) + "Affected Fragment : " + closestFragment.getId() );

					}
					if(closestFragment.getParent()==null){
						// The root node is affected which we know is not the same as the other .. So return to optimize
//						return affectedFragments;
					}
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return affectedFragments;
	}


	private void addToNearDuplicates(StateVertex state) {
		if( state ==null) {
			return;
		}
		boolean added = false;
		for(Set<StateVertex> set : nearDuplicates) {
			if(set.contains(state)) {
				added = true;
				break;
			}
		}
		if(!added) {
			Set<StateVertex> newSet = new HashSet<>();
			newSet.add(state);
			nearDuplicates.add(newSet);
		}
	}
	
	private boolean hasExploredNearDuplicate(StateVertex state) {
		if(state==null) {
			return false;
		}
		
		if(!state.hasUnexploredActions()) {
			return true;
		}
		
		if(state.hasNearDuplicate()) {
			for(Set<StateVertex> set : nearDuplicates) {
				if(set.contains(state)) {
					for(StateVertex nd : set) {
						if(!nd.hasUnexploredActions()) {
							LOG.info("{} has an explored near duplicate {}", state.getName(), nd.getName());
							return true;
						}
					}
					break;
				}
			}
			return false;
		}
		
		return false;
	}
	
	private void addToNearDuplicates(StateVertex newState, StateVertex expectedState) {
		newState.setHasNearDuplicate(true);
		expectedState.setHasNearDuplicate(true);
		
		// Add the lowest id as cluster number
		if(newState.getCluster() < expectedState.getCluster() ) {
			expectedState.setCluster(newState.getCluster());
		}
		else{
			newState.setCluster(expectedState.getCluster());
		}
		
		if(newState == null || expectedState ==null) {
			return;
		}
		boolean added = false;
		for(Set<StateVertex> set : nearDuplicates) {
			if(set.contains(newState) || set.contains(expectedState)) {
				set.add(newState);
				set.add(expectedState);
				added = true;
				break;
			}
		}
		if(!added) {
			Set<StateVertex> newSet = new HashSet<>();
			newSet.add(newState);
			newSet.add(expectedState);
			nearDuplicates.add(newSet);
		}
		if(newState.getRootFragment()!=null && expectedState.getRootFragment() !=null) {
			if(!newState.getRootFragment().isAccessTransferred()) {
				LOG.info("Transferring ND2 Access");
				newState.getRootFragment().addND2Fragment(expectedState.getRootFragment());
//				newState.getRootFragment().transferEquivalentAccess(expectedState.getRootFragment());
//				newState.getRootFragment().setAccessTransferred(true);
			}
		}
	}


	public List<Fragment> getAllFragments() {
		return this.fragments;
	}


	public void stopCrawling() {
		this.fragments = null;
		this.hops = null;
		this.nearDuplicates = null;
		this.stateComparisionCache = null;
		this.sfg = null;
	}


	public void cacheStateComparisions(StateVertex newState) {
		if(this.sfg == null)
			return;
		
		CopyOnWriteArrayList<StateVertex> allStates = new CopyOnWriteArrayList<>(sfg.get().getAllStates());

		for(StateVertex state: allStates) {
			if(state.getId() == newState.getId())
				continue;
			try {
				boolean assignDynamic = true;
				StateComparision comp = cacheStateComparision(newState, state, assignDynamic);
				LOG.info(state.getName()+ " : " + newState.getName() + " are " + comp);
			}catch(Exception ex) {
				LOG.error("Could not compare states {} and {}", state.getName(), newState.getName());
			}
		}
	}

	/**
	 * Uses xmlunit diffing
	 * Returns list of dom differences that are visible
	 * @param doc1
	 * @param doc2
	 * @param vips
	 * @return
	 */
	public static List<Difference> domDifferences(Document doc1, Document doc2, boolean vips){
		try {
			doc1 = DomUtils.asDocument(DomUtils.getDocumentToString(doc1));
			doc2 = DomUtils.asDocument(DomUtils.getDocumentToString(doc2));
			boolean offline = false;
			VipsUtils.cleanDom(doc1, offline);
			VipsUtils.cleanDom(doc2, offline);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Node head1 = doc1.getElementsByTagName("head").item(0);
		Node head2 = doc2.getElementsByTagName("head").item(0);
		
		head1.getParentNode().removeChild(head1);
		head2.getParentNode().removeChild(head2);
//		doc2.getElementsByTagName("head").item(0);
		List<Difference> returnList = new ArrayList<Difference>();
//		Predicate<Node> predicate = new ;
		Diff diffBuilder = DiffBuilder
		        .compare(doc1)
		        .withTest(doc2)
		        .ignoreComments()
//		        .withNodeFilter( predicate )
		        .ignoreWhitespace()
		        .build();
		Iterable<Difference> diffs = diffBuilder.getDifferences();
		for(Difference diff: diffs) {
			Node node = diff.getComparison().getControlDetails().getTarget();
			Node other = diff.getComparison().getTestDetails().getTarget();
			ComparisonType type = diff.getComparison().getType();
			ComparisonResult result = diff.getResult();
//			LOG.info("this {}", node);
//			LOG.info("other {}", other);
//			LOG.info("compType {}", type);
//			LOG.info("{}",result);
			if(node==null||other==null)
				continue;
			if(vips) {
				if(node instanceof Attr) {
					try {
						if(VipsUtils.isDisplayed(((Attr) node).getOwnerElement(), null)) {
							returnList.add(diff);
						}
					}catch(Exception ex) {
						LOG.error("Could not check visibility of {}", node);
						LOG.error(ex.getMessage());
					}
				}
				else {
					if(VipsUtils.isDisplayed( node, null)) {
						returnList.add(diff);
					}
				}
					
				
			}
		}
		
		return returnList;
	}

	public static List<Node> getDomDiffNodes(HybridStateVertexImpl stateVertex, HybridStateVertexImpl toCompare) {
		List<Node> domDiffNodes = new ArrayList<>();
		
		List<Difference> diffs = domDifferences(toCompare.getDocument(), stateVertex.getDocument(), true);
		
		for(Difference diff: diffs) {
			ComparisonType type = diff.getComparison().getType();
			boolean proceed = false;
			switch(type) {
				case ATTR_VALUE:
				case ELEMENT_NUM_ATTRIBUTES:
				case ATTR_NAME_LOOKUP:
					break;
				case TEXT_VALUE:
					proceed=true;
					break;
				default:
					proceed = true;
					break;
			}
			if(!proceed) {
//				LOG.info("Ignoring diff {}", diff);
				continue;
			}
//			if(diff.getComparison().getType().equals(ComparisonType.ELEMENT_NUM_ATTRIBUTES)) {
//				LOGGER.info("Ignoring diff of num attributes {}", diff);
//				continue;
//			}
			
			LOG.info("Diff type {}", diff.getComparison().getType());
			Node node = diff.getComparison().getControlDetails().getTarget();
			if(node!=null) {
				if(node.getNodeName().equalsIgnoreCase("#text")) {
					node = node.getParentNode();
				}
			}
			try {
				node = DomUtils.getElementByXpath(stateVertex.getDocument(), XPathHelper.getXPathExpression(node));
				domDiffNodes.add(node);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			Node node = diff.getComparison().getControlDetails().getTarget();
		}
		
		return domDiffNodes;
	}


	/**
	 * Updates no of times state has not been polled during crawl (Helps prevent crawler getting stuck in similar states)
	 * @param currentState
	 */
	public void seenState(StateVertex currentState) {
		if(!numNonSelections.containsKey(currentState.getId())) {
			numNonSelections.put(currentState.getId(), 0.0);
		}
		List<Integer> selectionStates = new ArrayList<Integer>();
		selectionStates.addAll(numNonSelections.keySet());
		for(int key: selectionStates) {
			if(key == currentState.getId()) {
				numNonSelections.replace(key, 0.0);
			}
			else {
				numNonSelections.replace(key, numNonSelections.get(key)+1);
			}
		}
	}


	public boolean areRelated(Fragment frag, Fragment frag2) {
		if(getRelatedFragments(frag).contains(frag2)) {
			LOG.info("Fragments {} {} and {} {} related", frag.getId(), frag.getReferenceState().getName(),
					frag2.getId(), frag2.getReferenceState().getName());
			return true;
		}
		if(frag.getNd2Fragments().contains(frag2)) {
			LOG.info("Fragments {} {} and {} {} ND2", frag.getId(), frag.getReferenceState().getName(),
					frag2.getId(), frag2.getReferenceState().getName());
			return true;
		}
		
		return false;
	}


	public static void setThresholds(FragmentRules fragRules) {
		USEFUL_FRAGMENT_RULES = fragRules;
	}


}
