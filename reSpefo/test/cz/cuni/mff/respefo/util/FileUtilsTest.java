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
	
	@Test
	public void testParentDirectoryUtils() {
		String path = "/adam/home/bla.txt";
		
		assertEquals("/adam/home", FileUtils.getParentDirectory(path));
		assertEquals("bla.txt", FileUtils.stripParent(path));
		
		path = "bla.txt";
		
		assertEquals("", FileUtils.getParentDirectory(path));
		assertEquals("bla.txt", FileUtils.stripParent(path));
	}
}
