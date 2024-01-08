package testmod.gametest_core.mixin;


import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.throwables.MixinException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(GameTestRegistry.class)
public interface GameTestRegistryAccessor {

    @Accessor("TEST_FUNCTIONS")
    static Collection<TestFunction> TEST_FUNCTIONS() {
        throw new MixinException("Mixin failed to apply");
    }

    @Accessor("TEST_CLASS_NAMES")
    static Set<String> TEST_CLASS_NAMES() {
        throw new MixinException("Mixin failed to apply");
    }

    @Accessor("BEFORE_BATCH_FUNCTIONS")
    static Map<String, Consumer<ServerLevel>> BEFORE_BATCH_FUNCTIONS() {
        throw new MixinException("Mixin failed to apply");
    }

    @Accessor("AFTER_BATCH_FUNCTIONS")
    static Map<String, Consumer<ServerLevel>> AFTER_BATCH_FUNCTIONS() {
        throw new MixinException("Mixin failed to apply");
    }

    @Accessor("LAST_FAILED_TESTS")
    static Collection<TestFunction> LAST_FAILED_TESTS() {
        throw new MixinException("Mixin failed to apply");
    }
}
