package com.almostreliable.unified.api;

import javax.annotation.Nullable;
import java.util.Collection;

public interface AlmostUnifiedRuntime {

    /**
     * Get a composed unify lookup from all existing unify handlers.
     *
     * @return The unify lookup
     */
    UnifyLookup getUnifyLookup();

    Collection<? extends UnifyHandler> getUnifyHandlers();

    @Nullable
    UnifyHandler getUnifyHandler(String name);

    /**
     * Get all available tag substitutions, which are defined by the user in the 'tags.json' config.
     *
     * @return All tag substitutions
     */
    TagSubstitutions getTagSubstitutions();

    /**
     * Get all available placeholders, which are defined by the user in the 'placeholders.json' config.
     *
     * @return All placeholders
     */
    Placeholders getPlaceholders();
}
