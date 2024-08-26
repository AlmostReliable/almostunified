package testmod;

import testmod.gametest_core.GameTestLoader;
import testmod.tests.ExampleTest;
import testmod.tests.GregTechModernRecipeUnifierTests;
import testmod.tests.LootUnificationTests;
import testmod.tests.ReplacementsTests;
import testmod.tests.ShapedRecipeUnifierTests;
import testmod.tests.SmithingRecipeUnifierTest;
import testmod.tests.UnificationHandlerTests;
import testmod.tests.UnifyTests;
import testmod.tests.core.TagInheritanceTests;
import testmod.tests.core.TagSubstitutionTests;

public class CommonTest {

    public static void init(boolean gametestEnabled) {
        if (gametestEnabled) {
            GameTestLoader.registerProviders(
                ExampleTest.class,
                UnificationHandlerTests.class,
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
