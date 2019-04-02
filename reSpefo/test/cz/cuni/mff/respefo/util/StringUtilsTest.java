package cz.cuni.mff.respefo.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {
	@Test
	public void testPaddedString() {
		String str = "abc";
		
		assertEquals("   abc", StringUtils.paddedString(str, 6));
		assertEquals("abc", StringUtils.paddedString(str, 3));
		assertEquals("abc", StringUtils.paddedString(str, 0));
	}
}
