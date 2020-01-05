package diergo.spring.legacy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;

import java.util.HashSet;
import java.util.Set;

abstract class AbstractRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware {

    private final Set<Integer> registriesPostProcessed = new HashSet<>();
    private final Set<Integer> factoriesPostProcessed = new HashSet<>();
    protected Environment environment;
    private int order;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        int registryId = System.identityHashCode(registry);
        if (!registriesPostProcessed.add(registryId)) {
            throw new IllegalStateException(
                    "postProcessBeanDefinitionRegistry already called on this post-processor against " + registry);
        }
        if (factoriesPostProcessed.contains(registryId)) {
            throw new IllegalStateException(
                    "postProcessBeanFactory already called on this post-processor against " + registry);
        }
        postProcess(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        int factoryId = System.identityHashCode(beanFactory);
        if (!factoriesPostProcessed.add(factoryId)) {
            throw new IllegalStateException(
                    "postProcessBeanFactory already called on this post-processor against " + beanFactory);
        }
        if (!this.registriesPostProcessed.contains(factoryId)) {
            // BeanDefinitionRegistryPostProcessor hook apparently not supported...
            // Simply call processConfigurationClasses lazily at this point then.
            postProcess((BeanDefinitionRegistry) beanFactory);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected abstract void postProcess(BeanDefinitionRegistry registry);
}
