package diergo.spring.legacy;

import example.legacy.*;
import example.spring.SpringBeanInjectedLegacy;
import example.spring.SpringConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

public class ExampleIntegrationTest {

    private AnnotationConfigApplicationContext applicationContext;

    @Test
    public void legacyCodeCanUseSpringBeansIfRefreshed() {
        LegacyCodeUsingSpring legacyCode = new LegacyCodeUsingSpring();
        assertThat(legacyCode.canUseSpring(), is(false));
        applicationContext.refresh();
        assertThat(legacyCode.canUseSpring(), is(true));
    }

    @Test
    public void legacySingletonsAreRegistered() {
        applicationContext.refresh();
        assertThat(applicationContext.getBean(LegacySingletonByField.class), notNullValue());
        assertThat(applicationContext.getBean(LegacySingletonByMethod.class), notNullValue());
    }

    @Test
    public void legacySingletonCanBeInjected() {
        applicationContext.refresh();
        assertThat(applicationContext.getBean(SpringBeanInjectedLegacy.class), notNullValue());
    }

    @Test
    public void legacyFactorySingletonIsAvailable() {
        applicationContext.refresh();
        CreatedSingleton first = applicationContext.getBean(CreatedSingleton.class);
        CreatedSingleton second = applicationContext.getBean(CreatedSingleton.class);
        assertThat(first, notNullValue());
        assertThat(first, sameInstance(second));
    }

    @Test
    public void legacyFactoryPrototypeIsAvailable() {
        applicationContext.refresh();
        CreatedPrototype first = applicationContext.getBean(CreatedPrototype.class);
        CreatedPrototype second = applicationContext.getBean(CreatedPrototype.class);
        assertThat(first, notNullValue());
        assertThat(second, notNullValue());
        assertThat(first, not(sameInstance(second)));
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
