package testmod.tests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import testmod.gametest_core.AlmostGameTestHelper;
import testmod.gametest_core.SimpleGameTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LootUnificationTests {

    private Block getBlock(String name) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(name)).orElseThrow();
    }

    /**
     * Test results are based on the testmod_configs -> materials.json
     *
     * @param helper - GameTestHelper
     */
    @SimpleGameTest
    public void simpleTest(AlmostGameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);

        ArrayList<BlockPos> positions = BlockPos
                .betweenClosedStream(new BlockPos(0, 2, 0), new BlockPos(2, 10, 2))
                .collect(Collectors.toCollection(ArrayList::new));

        testDrop(helper, player, pickaxe, positions.removeFirst(), "testmod:osmium_ore", "testmod:osmium_ingot");
        testDrop(helper, player, pickaxe, positions.removeFirst(), "meka_fake:osmium_ore", "testmod:osmium_ingot");
        testDrop(helper, player, pickaxe, positions.removeFirst(), "ie_fake:osmium_ore", "testmod:osmium_ingot");
        testDrop(helper, player, pickaxe, positions.removeFirst(), "thermal_fake:osmium_ore", "testmod:osmium_ingot");

        ItemStack pickaxe2 = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe2.enchant(helper.getHolder(Enchantments.SILK_TOUCH), 1);
        testDrop(helper, player, pickaxe2, positions.removeFirst(), "testmod:osmium_ore", "testmod:osmium_ore");
        testDrop(helper, player, pickaxe2, positions.removeFirst(), "meka_fake:osmium_ore", "testmod:osmium_ore");
        testDrop(helper, player, pickaxe2, positions.removeFirst(), "ie_fake:osmium_ore", "testmod:osmium_ore");
        testDrop(helper, player, pickaxe2, positions.removeFirst(), "thermal_fake:osmium_ore", "testmod:osmium_ore");
    }

    private void testDrop(AlmostGameTestHelper helper, Player player, ItemStack tool, BlockPos orePos, String oreId, String exceptedId) {
        Block oreBlock = getBlock(oreId);
        helper.setBlock(orePos, oreBlock.defaultBlockState());
        List<ItemStack> drops = Block.getDrops(helper.getBlockState(orePos),
                helper.getLevel(),
                orePos,
                null,
                player,
                tool);
        assertEquals(1, drops.size());
        var result = drops.getFirst().getItem();
        var resultId = BuiltInRegistries.ITEM.getKey(result);
        assertEquals(exceptedId, resultId.toString());
    }

}
