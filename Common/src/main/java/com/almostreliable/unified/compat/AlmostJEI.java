package com.almostreliable.unified.compat;


import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.BuildConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

@JeiPlugin
public class AlmostJEI implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(BuildConfig.MOD_ID, "jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jei) {
        if (AlmostUnifiedPlatform.INSTANCE.isModLoaded("rei")) {
            return;
        }

        Collection<ItemStack> items = HideHelper.createHidingList();
        if (!items.isEmpty()) {
            jei.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, items);
        }
    }
}
