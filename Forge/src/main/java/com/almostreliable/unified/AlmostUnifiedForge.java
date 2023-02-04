package com.almostreliable.unified;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(BuildConfig.MOD_ID)
public class AlmostUnifiedForge {

    public AlmostUnifiedForge() {
        if (!AlmostUnified.getStartupConfig().isServerOnly()) {
            var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener((RegisterEvent event) -> {
                if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
                    ForgeRegistries.RECIPE_SERIALIZERS.register(ClientRecipeTracker.ID, ClientRecipeTracker.SERIALIZER);
                }
                if (event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
                    ForgeRegistries.RECIPE_TYPES.register(ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
                }
            });
        }
    }
}
