package com.almostreliable.unified;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(BuildConfig.MOD_ID)
public class AlmostUnifiedForge {

    public AlmostUnifiedForge() {
        if (!AlmostUnified.getStartupConfig().isServerOnly()) {
            var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener((RegisterEvent event) -> {
                if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
                    Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                            ClientRecipeTracker.ID,
                            ClientRecipeTracker.SERIALIZER);
                }
                if (event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
                    Registry.register(BuiltInRegistries.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
                }
            });
        }
    }
}
