/**
 * 
 */
package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.Objects;

/**
 * Logic operations for conditions.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 */
@Immutable
public final class Logic {

	/**
	 * @param condition
	 *            the condition.
	 * @return the condition negated.
	 */
	public static Condition not(final Condition condition) {
	    return new Logic.Not(condition);
	}

	/**
	 * @param conditions
	 *            the conditions.
	 * @return AND of conditions
	 */
	public static Condition and(final Condition... conditions) {
		return new AbstractCondition() {

			@Override
			public boolean check(EmbeddedBrowser browser) {
				for (Condition condition : conditions) {
					if (!condition.check(browser)) {
						return false;
					}
				}
				return true;
			}
		};
	}

	/**
	 * @param conditions
	 *            the conditions.
	 * @return OR conditions
	 */
	public static Condition or(final Condition... conditions) {
		return new AbstractCondition() {

			@Override
			public boolean check(EmbeddedBrowser browser) {
				for (Condition condition : conditions) {
					if (condition.check(browser)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * @param conditions
	 *            the conditions.
	 * @return NAND conditions
	 */
	public static Condition nand(final Condition... conditions) {
		return new AbstractCondition() {

			@Override
			public boolean check(EmbeddedBrowser browser) {
				return not(and(conditions)).check(browser);
			}

		};
	}

	private Logic() {
	}
	
	private static class Not extends AbstractCondition {
	    private Condition condition;
	    
	    public Not(Condition c) {
	        condition = c;
	    }
	    
        @Override
        public boolean check(EmbeddedBrowser browser) {
            return !condition.check(browser);
        }
        
        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("condition", condition)
                    .toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(condition);
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Not) {
                Not that = (Not) object;
                return Objects.equal(this.condition, that.condition);
            }
            return false;
        }       
	}

}
