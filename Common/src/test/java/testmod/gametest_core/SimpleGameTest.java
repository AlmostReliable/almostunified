package testmod.gametest_core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for game tests. Can be used to create simple game tests. Simple game tests will automatically succeed if no exception is thrown.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleGameTest {
    String batch() default "defaultBatch";

    String template() default "";

    int attempts() default 1;
}
