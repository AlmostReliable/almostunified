package com.almostreliable.unified;

import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.StartupConfig;
import com.almostreliable.unified.impl.AlmostUnifiedRuntimeImpl;
import com.almostreliable.unified.loot.LootUnification;
import com.almostreliable.unified.recipe.RecipeUnifyHandler;
import com.almostreliable.unified.utils.CustomLogger;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@SuppressWarnings({ "UtilityClassWithoutPrivateConstructor", "StaticVariableUsedBeforeInitialization" })
public final class AlmostUnifiedCommon {

    public static final Logger LOGGER = CustomLogger.create();
    public static final StartupConfig STARTUP_CONFIG = Config.load(StartupConfig.NAME, StartupConfig.SERIALIZER);

    @Nullable private static AlmostUnifiedRuntime RUNTIME;

    @Nullable
    static AlmostUnifiedRuntime getRuntime() {
        return RUNTIME;
    }

    public static void onTagLoaderReload(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags) {
        RUNTIME = AlmostUnifiedRuntimeImpl.create(itemTags, blockTags);
    }


    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes, HolderLookup.Provider registries) {
        Preconditions.checkNotNull(RUNTIME, "AlmostUnifiedRuntime was not loaded correctly");

        if (RUNTIME instanceof RecipeUnifyHandler handler) {
            handler.run(recipes);
        } else {
            throw new IllegalStateException("Runtime is not a RecipeUnifyHandler");
        }

        LootUnification.unifyLoot(RUNTIME, registries);
    }
}