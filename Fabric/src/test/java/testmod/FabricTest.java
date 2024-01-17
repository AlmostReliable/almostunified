package testmod;

import net.fabricmc.api.ModInitializer;
import testmod.gametest_core.GameTestLoader;
import testmod.tests.AmethystImbuementRecipeUnifierTests;
import testmod.tests.ModernIndustrializationRecipeUnifierTests;

public class FabricTest implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonTest.init("true".equals(System.getProperty("fabric-api.gametest")));
        GameTestLoader.registerProviders(AmethystImbuementRecipeUnifierTests.class,
                ModernIndustrializationRecipeUnifierTests.class);
    }
}
