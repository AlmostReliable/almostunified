package testmod;

import net.fabricmc.api.ModInitializer;

public class FabricTest implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonTest.init("true".equals(System.getProperty("fabric-api.gametest")));
    }
}
