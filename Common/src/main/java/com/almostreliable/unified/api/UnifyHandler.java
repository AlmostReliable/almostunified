package com.almostreliable.unified.api;

import net.minecraft.world.item.Item;

public interface UnifyHandler extends UnifySettings, UnifyLookup {

    TagMap<Item> getTagMap();
}
