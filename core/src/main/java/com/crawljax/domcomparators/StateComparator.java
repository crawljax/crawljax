package com.crawljax.domcomparators;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateComparator {

	private static final Logger LOG = LoggerFactory.getLogger(StateComparator.class);

	@VisibleForTesting
	public static StateComparator noStrippingComparator() {
		return new StateComparator(ImmutableList.<DomStripper>of());
	}

	private final ImmutableList<DomStripper> strippers;

	public StateComparator(ImmutableList<DomStripper> strippers) {
		this.strippers = strippers;
	}

	public String getStrippedDom(EmbeddedBrowser browser) {
		String dom = browser.getUnStrippedDom();
		for (DomStripper stripper : strippers) {
			LOG.debug("Stripping the dom with {}", stripper);
			dom = stripper.apply(dom);
		}
		return dom;
	}
}
