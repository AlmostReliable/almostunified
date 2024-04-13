package testmod.tests.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import testmod.gametest_core.AlmostGameTestHelper;
import testmod.gametest_core.SimpleGameTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TagInheritanceTests {

    @SimpleGameTest
    public void testTagInheritance(AlmostGameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, Blocks.BEACON);
        BeaconBlockEntity be = helper.getBlockEntity(pos, BeaconBlockEntity.class);
        Player player = helper.makeMockPlayer();
        var menu = (BeaconMenu) be.createMenu(42, player.getInventory(), player);
        assert menu != null;
        Slot slot = menu.getSlot(0);

        assertFalse(slot.mayPlace(Items.STICK.getDefaultInstance()));

        // Both mod a and mod b silver should be placeable because of default set through `tags` json
        var mod_a_silver = BuiltInRegistries.ITEM.get(new ResourceLocation("mod_a:silver_ore"));
        assertTrue(slot.mayPlace(mod_a_silver.getDefaultInstance()));
        var mod_b_silver = BuiltInRegistries.ITEM.get(new ResourceLocation("mod_b:silver_ore"));
        assertTrue(slot.mayPlace(mod_b_silver.getDefaultInstance()));

        // mod c silver should be placeable because of tag inheritance
        var mod_c_silver = BuiltInRegistries.ITEM.get(new ResourceLocation("mod_c:silver_ore"));
        assertTrue(slot.mayPlace(mod_c_silver.getDefaultInstance()));
    }
}
