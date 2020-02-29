package cz.cuni.mff.respefo.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {
	@Test
	public void testPaddedString() {
		String str = "abc";
		
		assertEquals("   abc", StringUtils.trimmedOrPaddedString(str, 6));
		assertEquals("abc", StringUtils.trimmedOrPaddedString(str, 3));
		assertEquals("ab", StringUtils.trimmedOrPaddedString(str, 2));
	}
}
