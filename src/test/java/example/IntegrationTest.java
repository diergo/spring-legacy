package example;

import example.legacy.LegacyCodeUsingSpring;
import example.spring.SpringConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IntegrationTest {

    private static AnnotationConfigApplicationContext applicationContext;

    @Test
    void legacyCodeCanUseSpringBeansIfRefreshed() {
        LegacyCodeUsingSpring legacyCode = new LegacyCodeUsingSpring();
        assertThat(legacyCode.canUseSpring(), is(false));
        applicationContext.refresh();
        assertThat(legacyCode.canUseSpring(), is(true));
    }

    @BeforeAll
    static void createSpringContext() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SpringConfig.class);
    }

    @AfterAll
    static void closeSpringContext() {
        applicationContext.close();
        applicationContext = null;
    }
}
