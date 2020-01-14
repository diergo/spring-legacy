package diergo.spring.legacy;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A post processor registering all legacy singletons as spring beans.
 * It will check the {@linkplain #scanForSingletons(String...) basePackages} for any types with static methods as
 * {@linkplain Builder#singletonsFromStaticMethods(String...) singletons},
 * {@linkplain Builder#prototypesFromStaticMethods(String...)} prototypes}
 * or {@linkplain Builder#singletonsFromStaticFields(String...) fields as singletons}.
 */
public class LegacyBeanRegistryPostProcessor extends AbstractRegistryPostProcessor {

    /**
     * Build a post processor scanning the base packages passed.
     * If no packages are passed, the package of the caller is used.
     */
    public static Builder scanForSingletons(String... basePackages) {
        if (basePackages.length == 0) {
            StackTraceElement caller = new Throwable().getStackTrace()[1];
            return new Builder(ClassUtils.getPackageName(caller.getClassName()));
        }
        return new Builder(basePackages);
    }

    private final Builder builder;

    private LegacyBeanRegistryPostProcessor(Builder builder) {
        this.builder = builder;
        setOrder(builder.order);
    }

    @Override
    protected void postProcess(BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new LegacyClassPathBeanDefinitionScanner(registry, false, environment,
                this::customizeBeanDefinition);
        scanner.setBeanNameGenerator(builder.beanNameGenerator);
        builder.included.forEach(scanner::addIncludeFilter);
        scanner.scan(builder.basePackages);
    }

    private void customizeBeanDefinition(BeanDefinition bd) {
        builder.included.stream()
                .filter(included -> included.match(bd))
                .findFirst()
                .ifPresent(included -> included.customize(bd));
    }

    public static class Builder {

        private final String[] basePackages;
        private final List<CustomizingTypeFilter<?>> included = new ArrayList<>();
        private BeanNameGenerator beanNameGenerator = BeanDefinitionReaderUtils::generateBeanName;
        private int order = Ordered.LOWEST_PRECEDENCE;

        private Builder(String... basePackages) {
            this.basePackages = basePackages;
        }

        /**
         * Use a different bean name generator.
         *
         * @see BeanDefinitionReaderUtils#generateBeanName(BeanDefinition, BeanDefinitionRegistry)
         */
        public Builder beanNaming(BeanNameGenerator beanNameGenerator) {
            this.beanNameGenerator = beanNameGenerator;
            return this;
        }

        /**
         * Adjust the order of the {@link #asPostProcessor()}.
         *
         * @see org.springframework.core.PriorityOrdered
         */
        public Builder ordered(int order) {
            this.order = order;
            return this;
        }

        /**
         * Include singletons defined by non private static final fields of the declaring type.
         * Field names may be used to restrict the recognized fields.
         */
        public Builder singletonsFromStaticFields(String... fieldNames) {
            included.add(new LegacySingletonFieldFilter(fieldNames));
            return this;
        }

        /**
         * Include singletons defined by non private static final fields of the declaring type.
         * Field names may be used to restrict the recognized pattern.
         */
        public Builder singletonsFromStaticFields(Pattern fieldName) {
            included.add(new LegacySingletonFieldFilter(fieldName));
            return this;
        }

        /**
         * Include singletons defined by non private static final getters of the declaring type.
         * Method names may be used to restrict the recognized methods.
         */
        public Builder singletonsFromStaticMethods(String... methodNames) {
            included.add(new LegacyBeanMethodFilter(false, methodNames));
            return this;
        }

        /**
         * Include singletons defined by non private static final getters of the declaring type.
         * Method names may be used to restrict the recognized pattern.
         */
        public Builder singletonsFromStaticMethods(Pattern methodName) {
            included.add(new LegacyBeanMethodFilter(false, methodName));
            return this;
        }

        /**
         * Include prototypes defined by non private static final getters of the declaring type.
         * Method names may be used to restrict the recognized methods.
         */
        public Builder prototypesFromStaticMethods(String... methodNames) {
            included.add(new LegacyBeanMethodFilter(true, methodNames));
            return this;
        }

        /**
         * Include prototypes defined by non private static final getters of the declaring type.
         * Method names may be used to restrict the recognized pattern.
         */
        public Builder prototypesFromStaticMethods(Pattern methodName) {
            included.add(new LegacyBeanMethodFilter(true, methodName));
            return this;
        }

        /**
         * Create the post processor as configured by the builder.
         * If neither {@link #singletonsFromStaticFields(String...)} nor {@link #singletonsFromStaticMethods(String...)} has been called
         * any static methods and fields will be recognized.
         *
         * @see BeanDefinitionRegistryPostProcessor
         */
        public LegacyBeanRegistryPostProcessor asPostProcessor() {
            if (included.isEmpty()) {
                singletonsFromStaticMethods();
                singletonsFromStaticFields();
            }
            return new LegacyBeanRegistryPostProcessor(this);
        }
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
