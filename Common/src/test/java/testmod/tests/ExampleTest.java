package testmod.tests;

import net.minecraft.gametest.framework.GameTest;
import testmod.gametest_core.AlmostGameTestHelper;
import testmod.gametest_core.GameTestProvider;

public class ExampleTest implements GameTestProvider {

    @GameTest
    public void foo(AlmostGameTestHelper helper) {
        helper.succeedIf(() -> {

        });
    }
}
