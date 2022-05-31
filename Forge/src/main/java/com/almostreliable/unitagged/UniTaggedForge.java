package com.almostreliable.unitagged;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BuildConfig.MOD_ID)
public class UniTaggedForge extends UniTagged {

    public UniTaggedForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
    }

    private void onSetup(FMLCommonSetupEvent event) {
        UniTagged.initializeRuntime();
    }
}
