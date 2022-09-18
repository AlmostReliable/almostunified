package com.almostreliable.unified;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(BuildConfig.MOD_ID)
public class AlmostUnifiedForge {

    public AlmostUnifiedForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registry.RECIPE_SERIALIZER_REGISTRY)) {
                ForgeRegistries.RECIPE_SERIALIZERS.register(ClientRecipeTracker.ID, ClientRecipeTracker.SERIALIZER);
            }

            if (event.getRegistryKey().equals(Registry.RECIPE_TYPE_REGISTRY)) {
                ForgeRegistries.RECIPE_TYPES.register(ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
            }
        });
    }

}
