package diergo.spring.legacy;

import example.legacy.*;
import example.spring.SpringBeanInjectedLegacy;
import example.spring.SpringConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.*;
import sun.jvm.hotspot.ui.tree.CTypeTreeNodeAdapter;

import static diergo.spring.legacy.LegacyBeanRegistryPostProcessorBuilder.legacyPackages;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ExampleDefaultsIntegrationTest {

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
    public void legacyFactoryIsNotAvailable() {
        applicationContext.refresh();
        assertThat(applicationContext.getBeansOfType(CreatedPrototype.class), anEmptyMap());
        assertThat(applicationContext.getBeansOfType(CreatedSingleton.class), anEmptyMap());
    }

    @BeforeEach
    void createSpringContext() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(DefaultSpringConfig.class);
    }

    @AfterEach
    void closeSpringContext() {
        applicationContext.close();
    }

    @Configuration
    @ComponentScan(basePackages = "example.spring", excludeFilters = @ComponentScan.Filter(
            type =  FilterType.ASSIGNABLE_TYPE, classes = SpringConfig.class
    ))
    @Import(LegacySpringAccess.class)
    public static class DefaultSpringConfig {

        @Bean
        static BeanDefinitionRegistryPostProcessor legacySingletons() {
            return legacyPackages("example").build();
        }
    }
}
