package cz.cuni.mff.respefo.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {
	@Test
	public void testPaddedString() {
		String str = "abc";
		
		assertEquals("   abc", StringUtils.paddedString(str, 6));
		assertEquals("abc", StringUtils.paddedString(str, 3));
		assertEquals("abc", StringUtils.paddedString(str, 0));
	}
}
