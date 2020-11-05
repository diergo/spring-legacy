package diergo.spring.legacy;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;

/**
 * Callback for customizing a given bean definition if the current state is supported.
 */
public interface SmartBeanDefinitionCustomizer extends BeanDefinitionCustomizer {

    /**
     * Determine whether this customizer actually supports the given bean definition.
     * The customization should only be called when this method returns {@code true} before.
     *
     * @see #customize(BeanDefinition)
     */
    boolean supports(BeanDefinition bd);
}
