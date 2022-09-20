package com.almostreliable.unified;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BuildConfig.MOD_ID)
public class AlmostUnifiedForge {

    public AlmostUnifiedForge() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipe);
    }

    private void registerRecipe(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().register(ClientRecipeTracker.SERIALIZER);
        Registry.register(Registry.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
    }
}
