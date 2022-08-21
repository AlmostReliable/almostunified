package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.UnifyConfig;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class AlmostREI implements REIClientPlugin {
    @Override
    public void registerEntries(EntryRegistry registry) {
        if (AlmostUnifiedPlatform.INSTANCE.isModLoaded("jei")) {
            return;
        }

        UnifyConfig config = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        if(config.reiOrJeiDisabled()) {
            return;
        }

        HideHelper.createHidingList(config).stream().map(EntryStacks::of).forEach(registry::removeEntry);
    }
}
