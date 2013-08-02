import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class RegexTest {

	@Test
	public void test() {
		Pattern pattern = Pattern.compile("(?i)(?=.*?(?:master|wip/)).*");

		Assert.assertTrue(pattern.matcher("origin/master").matches());
		Assert.assertTrue(pattern.matcher("origin/WIP/bla").matches());
		Assert.assertTrue(pattern.matcher("origin/wip/bla222").matches());
		Assert.assertFalse(pattern.matcher("origin/feature/bla222").matches());
		Assert.assertFalse(pattern.matcher("origin/bla222").matches());
		Assert.assertFalse(pattern.matcher("origin/bug/bla222").matches());
		Assert.assertFalse(pattern.matcher("origin/wiplash").matches());
	}
}
