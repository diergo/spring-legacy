package example;

import example.legacy.LegacyCodeUsingSpring;
import example.legacy.LegacySingletonByField;
import example.legacy.LegacySingletonByMethod;
import example.spring.SpringConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class IntegrationTest {

    private AnnotationConfigApplicationContext applicationContext;

    @Test
    void legacyCodeCanUseSpringBeansIfRefreshed() {
        LegacyCodeUsingSpring legacyCode = new LegacyCodeUsingSpring();
        assertThat(legacyCode.canUseSpring(), is(false));
        applicationContext.refresh();
        assertThat(legacyCode.canUseSpring(), is(true));
    }

    @Test
    void legacySingletonsAreRegistered() {
        applicationContext.refresh();
        assertThat(applicationContext.getBean(LegacySingletonByField.class), notNullValue());
        assertThat(applicationContext.getBean(LegacySingletonByMethod.class), notNullValue());
    }

    @BeforeEach
    void createSpringContext() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SpringConfig.class);
    }

    @AfterEach
    void closeSpringContext() {
        applicationContext.close();
    }
}
