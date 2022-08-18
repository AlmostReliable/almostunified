package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class AlmostREI implements REIClientPlugin {
    @Override
    public void registerEntries(EntryRegistry registry) {
        if (AlmostUnifiedPlatform.INSTANCE.isModLoaded("jei")) {
            return;
        }

        HideHelper.createHidingList().stream().map(EntryStacks::of).forEach(registry::removeEntry);
    }
}
