package testmod;

import testmod.gametest_core.GameTestLoader;
import testmod.tests.ExampleTest;
import testmod.tests.JsonWalkerTests;

public class CommonTest {

    public static void init(boolean gametestEnabled) {
        if(gametestEnabled) {
            GameTestLoader.registerProviders(ExampleTest.class, JsonWalkerTests.class);
        }
    }
}
