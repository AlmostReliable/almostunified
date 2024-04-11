package testmod;

import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;

public class TestItems {

    public static void registerItems(BiConsumer<String, Item> register) {
        register.accept("testmod:osmium_ingot", new Item(new Item.Properties()));
        register.accept("meka_fake:osmium_ingot", new Item(new Item.Properties()));
        register.accept("ie_fake:osmium_ingot", new Item(new Item.Properties()));
        register.accept("thermal_fake:osmium_ingot", new Item(new Item.Properties()));


        register.accept("mod_a:silver_ore", new Item(new Item.Properties()));
        register.accept("mod_b:silver_ore", new Item(new Item.Properties()));
        register.accept("mod_c:silver_ore", new Item(new Item.Properties()));
    }

}
