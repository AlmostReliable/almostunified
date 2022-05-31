package com.almostreliable.unitagged.api;

import javax.annotation.Nullable;

public interface ReplacementLookupHelper {

    @Nullable
    String findReplacement(String id);
}
