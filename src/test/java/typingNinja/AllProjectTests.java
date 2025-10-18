package typingNinja;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Convenience suite that runs every test class under {@code src/test/java}.
 * Execute this suite from the IDE to trigger the entire test set.
 */
@Suite
@SelectPackages("typingNinja.tests")
public class AllProjectTests {
    // No code needed – annotations drive execution.
}
