package diergo.spring.legacy;

import example.legacy.CreatedPrototype;
import example.legacy.CreatedSingleton;
import example.legacy.LegacyFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

class LegacyFactoryBeanScannerTest {

    private SimpleBeanDefinitionRegistry registry;

    @Test
    void missingFactoryBeanDefinitionRaisesFatalBeanException() {
        LegacyFactoryBeanScanner tested = new LegacyFactoryBeanScanner(() -> LegacyFactoryBean.class,
                method -> true, SCOPE_SINGLETON);

        assertThrows(FatalBeanException.class, () -> tested.apply(registry));
    }

    @Test
    void singletonFactoryBeanDefinitionIsCreated() {
        LegacyFactoryBeanScanner tested = new LegacyFactoryBeanScanner(() -> LegacyFactoryBean.class,
                method -> method.getName().startsWith("get"), SCOPE_SINGLETON);
        registerFactoryBean();

        List<BeanDefinition> actual = tested.apply(registry).collect(toList());

        assertThat(actual, hasSize(1));
        BeanDefinition bd = actual.get(0);
        assertThat(bd.getFactoryBeanName(), is("factory"));
        assertThat(bd.getFactoryMethodName(), is("getSingleton"));
        assertThat(bd.getBeanClassName(), is(CreatedSingleton.class.getName()));
        assertThat(bd.getScope(), is(SCOPE_SINGLETON));
    }

    @Test
    void prototypeFactoryBeanDefinitionIsCreated() {
        LegacyFactoryBeanScanner tested = new LegacyFactoryBeanScanner(() -> LegacyFactoryBean.class,
                method -> method.getName().startsWith("create"), SCOPE_PROTOTYPE);
        registerFactoryBean();

        List<BeanDefinition> actual = tested.apply(registry).collect(toList());

        assertThat(actual, hasSize(1));
        BeanDefinition bd = actual.get(0);
        assertThat(bd.getFactoryBeanName(), is("factory"));
        assertThat(bd.getFactoryMethodName(), is("createPrototype"));
        assertThat(bd.getBeanClassName(), is(CreatedPrototype.class.getName()));
        assertThat(bd.getScope(), is(SCOPE_PROTOTYPE));
    }

    @BeforeEach
    void createRegistry() {
        registry = new SimpleBeanDefinitionRegistry();
    }

    private void registerFactoryBean() {
        registry.registerBeanDefinition("factory", new RootBeanDefinition(LegacyFactoryBean.class));
    }
}