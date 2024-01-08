package testmod.gametest_core;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import testmod.gametest_core.mixin.GameTestHelperAccessor;
import testmod.gametest_core.mixin.GameTestRegistryAccessor;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameTestLoader {

    public static final String ENABLED_NAMESPACES = "almostlib.gametest.testPackages";

    @Nullable
    private static List<Pattern> ENABLED_MODS;

    private GameTestLoader() {}

    public static void registerProvider(GameTestProvider provider) {
        for (Method method : provider.getClass().getDeclaredMethods()) {
            GameTest gametest = method.getAnnotation(GameTest.class);
            if (gametest != null) {
                register(provider, method, gametest);
            }
        }
    }

    @SafeVarargs
    public static void registerProviders(Class<? extends GameTestProvider>... providerClasses) {
        for (Class<? extends GameTestProvider> providerClass : providerClasses) {
            try {
                var constructor = providerClass.getConstructor();
                var instance = constructor.newInstance();
                if (!isAllowedModIdToRun(instance)) continue;
                registerProvider(instance);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void register(GameTestProvider provider, Method method, GameTest gametest) {
        String template = gametest.template();
        if (template.isEmpty()) {
            template = "testmod:empty_test_structure";
        }

        Rotation rotation = StructureUtils.getRotationForRotationSteps(gametest.rotationSteps());

        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName().toLowerCase();
        String classNameLower = className.toLowerCase();

        var test = new TestFunction(
                gametest.batch(),
                classNameLower + "." + methodName,
                template,
                rotation,
                gametest.timeoutTicks(),
                gametest.setupTicks(),
                gametest.required(),
                gametest.requiredSuccesses(),
                gametest.attempts(),
                convertMethodToConsumer(provider, method)
        );

        GameTestRegistryAccessor.TEST_FUNCTIONS().add(test);
        GameTestRegistryAccessor.TEST_CLASS_NAMES().add(className);
    }

    private static Consumer<GameTestHelper> convertMethodToConsumer(GameTestProvider provider, Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new RuntimeException("Static methods are not supported");
        }

        return testHelper -> {
            try {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalStateException("Method must have exactly one parameter");
                }

                //noinspection CastToIncompatibleInterface
                AlmostGameTestHelper almostHelper = new AlmostGameTestHelper(((GameTestHelperAccessor) testHelper).getTestInfo());
                method.invoke(provider, almostHelper);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static boolean isAllowedModIdToRun(GameTestProvider provider) {
        if (ENABLED_MODS == null) {
            String enabledNamespaces = System.getProperty(ENABLED_NAMESPACES);
            if (enabledNamespaces == null) {
                ENABLED_MODS = Collections.emptyList();
            } else {
                ENABLED_MODS = Arrays.stream(enabledNamespaces.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Pattern::compile)
                        .toList();
//                AlmostLib.LOGGER.info("Enabled gametests for mods: " + ENABLED_MODS);
            }
        }

        String name = provider.getClass().getName();
        return ENABLED_MODS.stream().map(p -> p.matcher(name)).anyMatch(Matcher::matches);
    }
}
