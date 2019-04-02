package cz.cuni.mff.respefo.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileUtilsTest {
	@Test
	public void testStripExtension() {
		String fileName = "foo/bar/expected.extension";
		assertEquals("foo/bar/expected", FileUtils.stripFileExtension(fileName));
		
		fileName = "noExtension";
		assertEquals(fileName, FileUtils.stripFileExtension(fileName));
		
		fileName = "more.than.one.extension";
		assertEquals("more.than.one", FileUtils.stripFileExtension(fileName));
	}
}
