package testmod.gametest_core;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.Nullable;
import testmod.gametest_core.mixin.GameTestHelperAccessor;
import testmod.gametest_core.mixin.GameTestRegistryAccessor;

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

    public static final String ENABLED_NAMESPACES = "almostunified.gametest.testPackages";

    @Nullable
    private static List<Pattern> ENABLED_MODS;

    private GameTestLoader() {}

    public static void registerProviders(Class<?>... providerClasses) {
        for (var providerClass : providerClasses) {
            if (!isAllowedModIdToRun(providerClass)) continue;

            Object instance;
            try {
                instance = providerClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            for (Method method : providerClass.getDeclaredMethods()) {
                GameTest gametest = method.getAnnotation(GameTest.class);
                SimpleGameTest simpleGametest = method.getAnnotation(SimpleGameTest.class);
                if (gametest != null && simpleGametest != null) {
                    throw new IllegalArgumentException(
                            "Cannot have both @GameTest and @SimpleGameTest on the same method");
                }

                if (gametest != null) {
                    register(method, gametest, TestMethodConsumer.of(instance, method)::accept);
                }

                if (simpleGametest != null) {
                    register(method, simpleGametest, TestMethodConsumer.of(instance, method)::acceptAutoSucceed);
                }
            }
        }
    }


    private static void register(Method method, GameTest gametest, Consumer<GameTestHelper> consumer) {
        String template = gametest.template();
        if (template.isEmpty()) {
            template = "testmod:empty_test_structure";
        }

        var test = new TestFunction(
                gametest.batch(),
                createTestName(method),
                template,
                StructureUtils.getRotationForRotationSteps(gametest.rotationSteps()),
                gametest.timeoutTicks(),
                gametest.setupTicks(),
                gametest.required(),
                gametest.manualOnly(),
                gametest.attempts(),
                gametest.requiredSuccesses(),
                gametest.skyAccess(),
                consumer
        );

        GameTestRegistryAccessor.TEST_FUNCTIONS().add(test);
        GameTestRegistryAccessor.TEST_CLASS_NAMES().add(method.getDeclaringClass().getSimpleName());
    }

    private static void register(Method method, SimpleGameTest gametest, Consumer<GameTestHelper> consumer) {
        String template = gametest.template();
        if (template.isEmpty()) {
            template = "testmod:empty_test_structure";
        }

        var test = new TestFunction(gametest.batch(),
                createTestName(method),
                template,
                Rotation.NONE,
                100,
                0,
                true,
                false,
                gametest.attempts(),
                1,
                false,
                consumer);

        GameTestRegistryAccessor.TEST_FUNCTIONS().add(test);
        GameTestRegistryAccessor.TEST_CLASS_NAMES().add(method.getDeclaringClass().getSimpleName());
    }

    private static String createTestName(Method method) {
        String className = method.getDeclaringClass().getSimpleName().toLowerCase();
        String methodName = method.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return className + "." + methodName;
    }

    private static boolean isAllowedModIdToRun(Class<?> provider) {
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

        String name = provider.getName();
        return ENABLED_MODS.stream().map(p -> p.matcher(name)).anyMatch(Matcher::matches);
    }

    private record TestMethodConsumer(Object instance, Method method) {

        static TestMethodConsumer of(Object instance, Method method) {
            if (Modifier.isStatic(method.getModifiers())) {
                throw new RuntimeException("static methods are not supported");
            }

            return new TestMethodConsumer(instance, method);
        }

        private String describeError(Throwable throwable) {
            if (throwable.getCause() != null) {
                return describeError(throwable.getCause());
            }

            if (throwable.getMessage() == null) {
                return throwable.toString();
            }

            return throwable.getClass().getName() + ": " + throwable.getMessage();
        }

        public void acceptAutoSucceed(GameTestHelper testHelper) {
            accept(testHelper);
            testHelper.succeed();
        }

        public void accept(GameTestHelper testHelper) {
            try {
                unsafeAccept(testHelper);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof AssertionError ae) {
                    throw new GameTestAssertException(ae.getMessage());
                }

                throw new RuntimeException(describeError(e.getTargetException()));
            }
        }

        public void unsafeAccept(GameTestHelper testHelper) throws IllegalAccessException, InvocationTargetException {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                method.invoke(instance);
                return;
            }

            if (parameterTypes.length == 1) {
                if (!GameTestHelper.class.isAssignableFrom(parameterTypes[0])) {
                    throw new RuntimeException(
                            "unsupported parameter type, parameter must extend " + GameTestHelper.class.getName());
                }

                // noinspection CastToIncompatibleInterface
                AlmostGameTestHelper almostHelper = new AlmostGameTestHelper(((GameTestHelperAccessor) testHelper).getTestInfo());
                method.invoke(instance, almostHelper);
                return;
            }

            throw new RuntimeException("unsupported number of parameters, must be 0 or 1");
        }
    }
}
