package testmod;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import java.util.function.BiConsumer;

public class TestItems {

    public static void registerStuff(BiConsumer<String, Item> registerItem, BiConsumer<String, Block> registerBlock) {

        Block testmod$osmium_ingot = ore();
        registerBlock.accept("testmod:osmium_ore", testmod$osmium_ingot);
        registerItem.accept("testmod:osmium_ore", new BlockItem(testmod$osmium_ingot, new Item.Properties()));
        Block meka_fake$osmium_ingot = ore();
        registerBlock.accept("meka_fake:osmium_ore", meka_fake$osmium_ingot);
        registerItem.accept("meka_fake:osmium_ore", new BlockItem(meka_fake$osmium_ingot, new Item.Properties()));
        Block ie_fake$osmium_ingot = ore();
        registerBlock.accept("ie_fake:osmium_ore", ie_fake$osmium_ingot);
        registerItem.accept("ie_fake:osmium_ore", new BlockItem(ie_fake$osmium_ingot, new Item.Properties()));
        Block thermal_fake$osmium_ingot = ore();
        registerBlock.accept("thermal_fake:osmium_ore", thermal_fake$osmium_ingot);
        registerItem.accept("thermal_fake:osmium_ore", new BlockItem(thermal_fake$osmium_ingot, new Item.Properties()));

        registerItem.accept("testmod:osmium_ingot", new Item(new Item.Properties()));
        registerItem.accept("meka_fake:osmium_ingot", new Item(new Item.Properties()));
        registerItem.accept("ie_fake:osmium_ingot", new Item(new Item.Properties()));
        registerItem.accept("thermal_fake:osmium_ingot", new Item(new Item.Properties()));

        registerItem.accept("mod_a:silver_ore", new Item(new Item.Properties()));
        registerItem.accept("mod_b:silver_ore", new Item(new Item.Properties()));
        registerItem.accept("mod_c:silver_ore", new Item(new Item.Properties()));
    }

    private static Block ore() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties
                .of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 3.0F);
        return new DropExperienceBlock(ConstantInt.of(0), props);
    }
}
