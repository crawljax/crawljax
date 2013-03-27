package com.crawljax.core;

import lombok.Data;

import com.crawljax.core.state.Eventable;
import com.google.common.collect.ImmutableList;

/**
 * Represents a task that has to be run by a {@link CrawlTaskConsumer}.
 */
@Data
public class CrawlTask {

	private final ImmutableList<Eventable> eventables;

}
