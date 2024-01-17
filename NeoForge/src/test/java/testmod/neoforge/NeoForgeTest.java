package testmod.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHooks;
import testmod.CommonTest;
import testmod.gametest_core.GameTestLoader;
import testmod.neoforge.tests.*;

@Mod("testmod")
public class NeoForgeTest {

    public NeoForgeTest() {
        CommonTest.init(GameTestHooks.isGametestEnabled());
        GameTestLoader.registerProviders(ArsNouveauRecipeTests.class,
                MekanismRecipeUnifierTests.class,
                ImmersiveEngineeringRecipeUnifierTests.class,
                EnderIORecipeUnifierTests.class,
                IntegratedDynamicsRecipeUnifierTests.class);
    }
}
