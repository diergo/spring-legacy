package diergo.spring.legacy;

import example.legacy.LegacySingletonByField;
import example.legacy.LegacySingletonByMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.env.StandardEnvironment;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LegacySingletonsRegistryPostProcessorTest {

    private LegacySingletonsRegistryPostProcessor tested;

    @Test
    public void beansForSingletonsAreRegisteredOnPostProcessBeanDefinitionRegistry() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        tested.postProcessBeanDefinitionRegistry(registry);

        Set<BeanDefinition> definitions = getExampleBeanDefinitions(registry);
        assertThat(definitions, hasSize(2));

        assertThat(definitions, everyItem(hasProperty("singleton", is(true))));
        assertThat(definitions, everyItem(hasProperty("lazyInit", is(true))));
        Set<String> beanClassNames = definitions.stream().map(BeanDefinition::getBeanClassName).collect(toSet());
        assertThat(beanClassNames, hasItem(LegacySingletonByMethod.class.getName()));
        assertThat(beanClassNames, hasItem(LegacySingletonByField.class.getName()));
    }

    @Test
    public void beansAreAlsoRegisteredOnPostProcessBeanFactory() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        tested.postProcessBeanFactory(beanFactory);

        Set<BeanDefinition> definitions = getExampleBeanDefinitions(beanFactory);
        assertThat(definitions.isEmpty(), is(false));
    }

    @BeforeEach
    void createProcessor() {
        tested = LegacySingletonsRegistryPostProcessor.scanForSingletons("example")
                .beanNaming(new AnnotationBeanNameGenerator())
                .asPostProcessor();
        tested.setEnvironment(new StandardEnvironment());
    }

    private Set<BeanDefinition> getExampleBeanDefinitions(BeanDefinitionRegistry registry) {
        return Stream.of(registry.getBeanDefinitionNames())
                .map(registry::getBeanDefinition)
                .filter(bd -> bd.getBeanClassName() != null)
                .filter(bd -> bd.getBeanClassName().startsWith("example"))
                .collect(toSet());
    }
}
