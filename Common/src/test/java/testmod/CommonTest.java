package testmod;

import testmod.gametest_core.GameTestLoader;
import testmod.tests.ExampleTest;
import testmod.tests.JsonWalkerTests;
import testmod.tests.ReplacementMapTests;

public class CommonTest {

    public static void init(boolean gametestEnabled) {
        if(gametestEnabled) {
            GameTestLoader.registerProviders(ExampleTest.class, JsonWalkerTests.class, ReplacementMapTests.class);
        }
    }
}
