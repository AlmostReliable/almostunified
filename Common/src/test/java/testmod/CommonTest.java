package testmod;

import testmod.gametest_core.GameTestLoader;
import testmod.tests.*;
import testmod.tests.core.OwnershipTests;
import testmod.tests.core.TagInheritanceTests;

public class CommonTest {

    public static void init(boolean gametestEnabled) {
        if (gametestEnabled) {
            GameTestLoader.registerProviders(
                    ExampleTest.class,
                    UnifyLookupTests.class,
                    ReplacementsTests.class,
                    UnifyTests.class,
                    ShapedRecipeUnifierTests.class,
                    SmithingRecipeUnifierTest.class,
                    GregTechModernRecipeUnifierTests.class,
                    AdAstraRecipeUnifierTests.class,
                    OwnershipTests.class,
                    TagInheritanceTests.class
            );
        }
    }
}
