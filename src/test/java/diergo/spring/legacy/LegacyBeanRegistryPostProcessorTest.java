package diergo.spring.legacy;

import example.legacy.LegacyPrototypeByStaticMethod;
import example.legacy.LegacySingletonByField;
import example.legacy.LegacySingletonByMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

public class LegacyBeanRegistryPostProcessorTest {

    private LegacyBeanRegistryPostProcessor tested;

    @Test
    public void beansForSingletonsAreRegisteredOnPostProcessBeanDefinitionRegistry() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        tested.postProcessBeanDefinitionRegistry(registry);

        Map<String, BeanDefinition> definitions = getExampleBeanDefinitions(registry);
        assertThat(definitions.size(), is(greaterThan(0)));
    }

    @Test
    public void singletonFromStaticFieldHasBeenRegistered() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        tested.postProcessBeanDefinitionRegistry(registry);

        BeanDefinition actual = getExampleBeanDefinitions(registry).get("legacySingletonByField");

        assertThat(actual.getScope(), is(SCOPE_SINGLETON));
        assertThat(actual.getBeanClassName(), is(LegacySingletonByField.class.getName()));
    }

    @Test
    public void singletonFromStaticMethodHasBeenRegistered() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        tested.postProcessBeanDefinitionRegistry(registry);

        BeanDefinition actual = getExampleBeanDefinitions(registry).get("legacySingletonByMethod");

        assertThat(actual.getScope(), is(SCOPE_SINGLETON));
        assertThat(actual.getBeanClassName(), is(LegacySingletonByMethod.class.getName()));
    }

    @Test
    public void prototypeFromStaticMethodHasBeenRegistered() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        tested.postProcessBeanDefinitionRegistry(registry);

        BeanDefinition actual = getExampleBeanDefinitions(registry).get("legacyPrototypeByStaticMethod");

        assertThat(actual.getScope(), is(SCOPE_PROTOTYPE));
        assertThat(actual.getBeanClassName(), is(LegacyPrototypeByStaticMethod.class.getName()));
    }

    @Test
    public void beansAreAlsoRegisteredOnPostProcessBeanFactory() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        tested.postProcessBeanFactory(beanFactory);

        Map<String, BeanDefinition> definitions = getExampleBeanDefinitions(beanFactory);
        assertThat(definitions.isEmpty(), is(false));
    }

    @BeforeEach
    void createProcessor() {
        tested = LegacyBeanRegistryPostProcessor.scanForSingletons("example")
                .beanNaming(new AnnotationBeanNameGenerator())
                .singletonsFromStaticFields()
                .singletonsFromStaticMethods("getInstance")
                .prototypesFromStaticMethods("createInstance")
                .asPostProcessor();
        tested.setEnvironment(new StandardEnvironment());
    }

    private Map<String, BeanDefinition> getExampleBeanDefinitions(BeanDefinitionRegistry registry) {
        return Stream.of(registry.getBeanDefinitionNames())
                .map(name -> new BeanDefinitionHolder(registry.getBeanDefinition(name), name))
                .filter(holder -> holder.getBeanDefinition().getBeanClassName() != null
                        && holder.getBeanDefinition().getBeanClassName().startsWith("example"))
                .collect(toMap(BeanDefinitionHolder::getBeanName, BeanDefinitionHolder::getBeanDefinition));
    }
}
