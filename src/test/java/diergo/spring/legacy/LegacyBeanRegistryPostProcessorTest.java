package diergo.spring.legacy;

import example.legacy.LegacySingletonByField;
import example.legacy.LegacySingletonByMethod;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.Ordered;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.reflect.Member;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@ExtendWith(MockitoExtension.class)
public class LegacyBeanRegistryPostProcessorTest {

    private LegacyBeanRegistryPostProcessor tested;
    @Mock
    private CustomizingTypeFilter<Member> filter;

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
    public void beanDefinitionsAreCustomized() {
        Mockito.doAnswer(invocation -> {
            invocation.getArgument(0, BeanDefinition.class).setDependsOn("test");
            return null;
        }).when(filter).customize(any(BeanDefinition.class));
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        tested.postProcessBeanDefinitionRegistry(registry);

        BeanDefinition actual = getExampleBeanDefinitions(registry).get("legacySingletonByMethod");

        assertThat(actual.getDependsOn(), Matchers.arrayContaining("test"));
        assertThat(actual.getBeanClassName(), is(LegacySingletonByMethod.class.getName()));
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
        tested = new LegacyBeanRegistryPostProcessor(singletonList(filter),
                new AnnotationBeanNameGenerator(), Ordered.LOWEST_PRECEDENCE, "example");
        tested.setEnvironment(new StandardEnvironment());
        when(filter.match(any(MetadataReader.class), any(MetadataReaderFactory.class)))
                .thenReturn(true);
        when(filter.match(any(BeanDefinition.class)))
                .thenReturn(true);
    }

    private Map<String, BeanDefinition> getExampleBeanDefinitions(BeanDefinitionRegistry registry) {
        return Stream.of(registry.getBeanDefinitionNames())
                .map(name -> new BeanDefinitionHolder(registry.getBeanDefinition(name), name))
                .filter(holder -> holder.getBeanDefinition().getBeanClassName() != null
                        && holder.getBeanDefinition().getBeanClassName().startsWith("example"))
                .collect(toMap(BeanDefinitionHolder::getBeanName, BeanDefinitionHolder::getBeanDefinition));
    }
}
