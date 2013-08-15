package com.crawljax.condition;

import java.util.Arrays;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.Objects;

/**
 * Logic operations for conditions.
 */
@Immutable
public final class Logic {

	/**
	 * @param condition
	 *            the condition.
	 * @return the condition negated.
	 */
	public static Condition not(final Condition condition) {
		return new Not(condition);
	}

	/**
	 * @param conditions
	 *            the conditions.
	 * @return AND of conditions
	 */
	public static Condition and(final Condition... conditions) {
		return new And(conditions);
	}

	/**
	 * @param conditions
	 *            the conditions.
	 * @return OR conditions
	 */
	public static Condition or(final Condition... conditions) {
		return new Or(conditions);
	}

	/**
	 * @param conditions
	 *            the conditions.
	 * @return NAND conditions
	 */
	public static Condition nand(final Condition... conditions) {
		return not(and(conditions));
	}

	private Logic() {
	}

	private static class Not implements Condition {
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
			return Objects.hashCode(getClass(), condition);
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

	private static class And implements Condition {
		private Condition[] conditions;

		public And(Condition... cs) {
			conditions = cs;
		}

		@Override
		public boolean check(EmbeddedBrowser browser) {
			for (Condition condition : conditions) {
				if (!condition.check(browser)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
			        .add("condition", Arrays.deepToString(conditions))
			        .toString();
		}

		@Override
		public int hashCode() {
			int args = Objects.hashCode((Object[]) conditions);
			return Objects.hashCode(getClass(), args);
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof And) {
				And that = (And) object;
				return Arrays.equals(this.conditions, that.conditions);
			}
			return false;
		}
	}

	private static class Or implements Condition {
		private Condition[] conditions;

		public Or(Condition... cs) {
			conditions = cs;
		}

		@Override
		public boolean check(EmbeddedBrowser browser) {
			for (Condition condition : conditions) {
				if (condition.check(browser)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
			        .add("condition", Arrays.deepToString(conditions))
			        .toString();
		}

		@Override
		public int hashCode() {
			int args = Objects.hashCode((Object[]) conditions);
			return Objects.hashCode(getClass(), args);
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof Or) {
				Or that = (Or) object;
				return Arrays.equals(this.conditions, that.conditions);
			}
			return false;
		}
	}

}
