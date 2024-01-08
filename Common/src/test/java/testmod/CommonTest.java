package testmod;

import testmod.gametest_core.GameTestLoader;
import testmod.tests.ExampleTest;

public class CommonTest {

    public static void init(boolean gametestEnabled) {

        if(gametestEnabled) {
            GameTestLoader.registerProviders(ExampleTest.class);
        }
    }
}
