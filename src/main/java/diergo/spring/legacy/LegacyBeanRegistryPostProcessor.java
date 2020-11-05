package diergo.spring.legacy;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * A post processor registering all legacy singletons as spring beans.
 *
 * @see LegacyBeanRegistryPostProcessorBuilder
 */
class LegacyBeanRegistryPostProcessor extends AbstractRegistryPostProcessor {

    private final String[] basePackages;
    private final List<CustomizingTypeFilter<?>> included;
    private final List<Function<BeanDefinitionRegistry, Stream<BeanDefinition>>> factories;
    private final BeanNameGenerator beanNameGenerator;

    public LegacyBeanRegistryPostProcessor(List<CustomizingTypeFilter<?>> included, List<Function<BeanDefinitionRegistry, Stream<BeanDefinition>>> factories, BeanNameGenerator beanNameGenerator, int order, String... basePackages) {
        this.factories = factories;
        super.setOrder(order);
        this.basePackages = basePackages;
        this.included = included;
        this.beanNameGenerator = beanNameGenerator;
    }


    /**
     * Creates a {@link ClassPathBeanDefinitionScanner} with all type filters configured, scans the base packages
     * and register bean definitions created. Afterwards register bean definitions created from factories configured.
     */
    @Override
    protected void postProcess(BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new LegacyClassPathBeanDefinitionScanner(registry, false, environment,
                this::customizeBeanDefinition);
        scanner.setBeanNameGenerator(beanNameGenerator);
        included.forEach(scanner::addIncludeFilter);
        scanner.scan(basePackages);
        factories.stream()
                .flatMap(factory -> factory.apply(registry))
                .map(bd -> new BeanDefinitionHolder(bd, beanNameGenerator.generateBeanName(bd, registry)))
                .filter(bdh -> !registry.containsBeanDefinition(bdh.getBeanName()))
                .forEach(bdh -> BeanDefinitionReaderUtils.registerBeanDefinition(bdh, registry));
    }

    private void customizeBeanDefinition(BeanDefinition bd) {
        included.stream()
                .filter(included -> included.supports(bd))
                .findFirst()
                .ifPresent(included -> included.customize(bd));
    }

    private static class LegacyClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

        private final BeanDefinitionCustomizer additionalCustomizer;

        LegacyClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, BeanDefinitionCustomizer additionalCustomizer) {
            super(registry, useDefaultFilters, environment);
            this.additionalCustomizer = additionalCustomizer;
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            return metadata.isIndependent() && !metadata.isInterface();
        }

        @Override
        protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
            super.postProcessBeanDefinition(beanDefinition, beanName);
            additionalCustomizer.customize(beanDefinition);
        }
    }
}
