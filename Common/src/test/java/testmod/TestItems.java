package testmod;

import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;

public class TestItems {

    public static void registerItems(BiConsumer<String, Item> register) {
        register.accept("testmod:osmium_ingot", new Item(new Item.Properties()));
        register.accept("meka_fake:osmium_ingot", new Item(new Item.Properties()));
        register.accept("ie_fake:osmium_ingot", new Item(new Item.Properties()));
        register.accept("thermal_fake:osmium_ingot", new Item(new Item.Properties()));
    }

}
