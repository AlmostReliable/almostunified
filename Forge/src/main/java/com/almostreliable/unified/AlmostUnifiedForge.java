package com.almostreliable.unified;

import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;

@Mod(BuildConfig.MOD_ID)
public class AlmostUnifiedForge {

//    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
//            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BuildConfig.MOD_ID);
//    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(
//            Registry.RECIPE_TYPE_REGISTRY,
//            BuildConfig.MOD_ID
//    );
//
//    public AlmostUnifiedForge() {
//        RECIPE_SERIALIZERS.register(ClientRecipeTracker.ID.getPath(), () -> ClientRecipeTracker.SERIALIZER);
//        RECIPE_TYPES.register(ClientRecipeTracker.ID.getPath(), () -> ClientRecipeTracker.TYPE);
//        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
//        RECIPE_SERIALIZERS.register(modEventBus);
//        RECIPE_TYPES.register(modEventBus);
//    }

    public AlmostUnifiedForge() {
        var something = Items.DIAMOND_SWORD;
    }
}
