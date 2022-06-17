package com.almostreliable.unified.api;

import javax.annotation.Nullable;

public interface RecipeTransformContext {

    @Nullable
    String findReplacement(String id);
}
