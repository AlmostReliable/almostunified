package testmod.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.RegisterEvent;
import testmod.CommonTest;
import testmod.TestItems;
import testmod.gametest_core.GameTestLoader;
import testmod.neoforge.tests.*;

@Mod("testmod")
public class NeoForgeTest {

    public NeoForgeTest(IEventBus bus) {
        CommonTest.init(GameTestHooks.isGametestEnabled());
        GameTestLoader.registerProviders(ArsNouveauRecipeTests.class,
                MekanismRecipeUnifierTests.class,
                ImmersiveEngineeringRecipeUnifierTests.class,
                EnderIORecipeUnifierTests.class,
                IntegratedDynamicsRecipeUnifierTests.class);

        bus.addListener(this::onRegistry);
    }

    private static void registerItem(String str, Item item) {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.parse(str), item);
    }

    private static void registerBlock(String str, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.parse(str), block);
    }

    public void onRegistry(RegisterEvent event) {
        if (event.getRegistry() == BuiltInRegistries.BLOCK) {
            TestItems.registerStuff(NeoForgeTest::registerItem, NeoForgeTest::registerBlock);
        }
    }
}
