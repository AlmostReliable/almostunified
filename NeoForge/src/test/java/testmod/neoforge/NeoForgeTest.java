package testmod.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHooks;
import testmod.CommonTest;

@Mod("testmod")
public class NeoForgeTest {

    public NeoForgeTest() {
        CommonTest.init(GameTestHooks.isGametestEnabled());
    }
}
