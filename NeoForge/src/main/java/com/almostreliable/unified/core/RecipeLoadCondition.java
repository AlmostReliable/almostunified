package com.almostreliable.unified.core;

import com.almostreliable.unified.utils.Utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

public record RecipeLoadCondition(boolean conditionsMet) implements ICondition {

    public static final String ID = Utils.getRL("conditional").toString();
    public static final String CONDITIONS_MET = "conditions_met";
    public static final MapCodec<RecipeLoadCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.BOOL.fieldOf(CONDITIONS_MET).forGetter(RecipeLoadCondition::conditionsMet)
    ).apply(i, RecipeLoadCondition::new));

    @Override
    public boolean test(IContext iContext) {
        return conditionsMet;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
