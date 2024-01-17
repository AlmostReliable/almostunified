package testmod;

import testmod.gametest_core.GameTestLoader;
import testmod.tests.*;

public class CommonTest {

    public static void init(boolean gametestEnabled) {
        if (gametestEnabled) {
            GameTestLoader.registerProviders(
                    ExampleTest.class,
                    ReplacementMapTests.class,
                    UnifyTests.class,
                    ShapedRecipeUnifierTests.class,
                    SmithingRecipeUnifierTest.class,
                    GregTechModernRecipeUnifierTests.class,
                    AdAstraRecipeUnifierTests.class
            );
        }
    }
}
