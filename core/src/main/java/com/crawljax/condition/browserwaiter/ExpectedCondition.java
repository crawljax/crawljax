package com.crawljax.condition.browserwaiter;

import com.crawljax.browser.EmbeddedBrowser;
import net.jcip.annotations.ThreadSafe;

/**
 * Interface for defining conditions to wait for.
 *
 * @author dannyroest@gmail.com (Danny Roest)
 */
@ThreadSafe
public interface ExpectedCondition {

    /**
     * Is the expected condition satisfied.
     *
     * @param browser the browser to execute the check on
     * @return Whether the condition is satisfied.
     */
    boolean isSatisfied(EmbeddedBrowser browser);
}
