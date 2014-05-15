package com.crawljax.domcomparators;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomStrippers {

	private static final Logger LOG = LoggerFactory.getLogger(DomStrippers.class);

	@VisibleForTesting
	public static DomStrippers noStrippers() {
		return new DomStrippers(ImmutableList.<DomStripper>of(), null);
	}

	private final ImmutableList<DomStripper> strippers;
	private final ImmutableList<ValidDomStripper> validDomStrippers;

	public DomStrippers(ImmutableList<DomStripper> strippers,
			ImmutableList<ValidDomStripper> validDomStrippers) {
		this.strippers = strippers;
		this.validDomStrippers = validDomStrippers;
	}

	public String getStrippedDom(String originalDom) {
		String dom = originalDom;
		try {
			if (!validDomStrippers.isEmpty()) {
				Document document = Jsoup.parse(dom);
				for (ValidDomStripper stripper : validDomStrippers) {
					document = applySafely(stripper, document);
				}
				// Apply changes to String representation
 				dom = document.toString();
			}
			for (DomStripper stripper : strippers) {
				dom = applySafely(stripper, dom);
			}
		}
		catch (RuntimeException e) {
			LOG.warn("Error during DOM stripping: . Returning unstripped dom", e.getMessage());
			LOG.debug("StackTrace was", e);
		}
		return dom;
	}

	private <T> T applySafely(Function<T, T> stripper, T dom) {
		try {
			return stripper.apply(dom);
		}
		catch (RuntimeException e) {
			LOG.error("Error running DOM stripper {}: {}", stripper.getClass(), e.getMessage());
			LOG.debug("StackTrace", e);
			return dom;
		}
	}
}