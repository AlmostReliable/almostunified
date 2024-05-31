package com.almostreliable.unified.api;

import javax.annotation.Nullable;
import java.util.Collection;

public interface AlmostUnifiedRuntime {

    UnifyLookup getUnifyLookup();

    Collection<? extends UnifyHandler> getUnifyHandlers();

    @Nullable
    UnifyHandler getUnifyHandler(String name);

    TagOwnerships getTagOwnerships();
}
