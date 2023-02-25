package com.crawljax.core.configuration;

import com.crawljax.core.state.Eventable.EventType;

/**
 * Represents a form action, e.g. a link that is clicked that handles the form NOTE: In general
 * FormAction is not designed to be instantiated directly.
 *
 * @author DannyRoest@gmail.com (Danny Roest)
 */
public class FormAction {

  private CrawlElement crawlElement;

  /**
   *Returns this CrawlElement.
 @param tagName the tag name of the element
   * 
   */
  public CrawlElement beforeClickElement(String tagName) {
    this.crawlElement = new CrawlElement(EventType.click, tagName);
    return crawlElement;
  }

  public CrawlElement beforeEnterElement(String tagName) {
    this.crawlElement = new CrawlElement(EventType.enter, tagName);
    return crawlElement;
  }

  /**
   *Returns the crawlTag.
 
   */
  protected CrawlElement getCrawlElement() {
    return crawlElement;
  }

}
