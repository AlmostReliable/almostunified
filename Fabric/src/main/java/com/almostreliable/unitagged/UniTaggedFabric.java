package com.almostreliable.unitagged;

import net.fabricmc.api.ModInitializer;

public class UniTaggedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        UniTaggedCommon.init();
    }
}
