package testmod.tests;

import testmod.gametest_core.SimpleGameTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleTest {

    @SimpleGameTest
    public void testMaddinCanRun() {
        // Currently safe check that gradle conf is correct :')
        assertTrue(true);
    }

}
