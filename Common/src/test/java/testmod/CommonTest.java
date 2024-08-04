package testmod;

import testmod.gametest_core.GameTestLoader;
import testmod.tests.*;
import testmod.tests.core.TagInheritanceTests;
import testmod.tests.core.TagSubstitutionTests;

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
                    TagSubstitutionTests.class,
                    TagInheritanceTests.class,
                    LootUnificationTests.class
            );
        }
    }
}
