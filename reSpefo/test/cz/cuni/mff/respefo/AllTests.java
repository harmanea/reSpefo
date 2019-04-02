package cz.cuni.mff.respefo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.FileUtilsTest;
import cz.cuni.mff.respefo.util.MathUtilsTest;
import cz.cuni.mff.respefo.util.StringUtils;
import cz.cuni.mff.respefo.util.StringUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({MathUtilsTest.class, FileUtilsTest.class, StringUtilsTest.class})
public class AllTests {

}
