package diergo.spring.legacy;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * Builder to configure a post processor registering beans from legacy code.
 * @see LegacyBeanRegistryPostProcessor
 */
public class LegacyBeanRegistryPostProcessorBuilder {

    private static final Pattern GETTERS = Pattern.compile("get[A-Z].+");
    private static final Pattern CONSTANTS = Pattern.compile("[A-Z][A-Z0-9_]+");

    /**
     * Build a new post processor scanning the base packages passed.
     * If no packages are passed, the package of the caller is used.
     */
    public static LegacyBeanRegistryPostProcessorBuilder legacyPackages(String... basePackages) {
        if (basePackages.length == 0) {
            StackTraceElement caller = new Throwable().getStackTrace()[1];
            return new LegacyBeanRegistryPostProcessorBuilder(ClassUtils.getPackageName(caller.getClassName()));
        }
        return new LegacyBeanRegistryPostProcessorBuilder(basePackages);
    }

    private final String[] basePackages;
    private final List<CustomizingTypeFilter<?>> included = new ArrayList<>();
    private final List<Function<BeanDefinitionRegistry, Stream<BeanDefinition>>> factories = new ArrayList<>();
    private BeanNameGenerator beanNameGenerator = BeanDefinitionReaderUtils::generateBeanName;
    private int order = Ordered.LOWEST_PRECEDENCE;

    private LegacyBeanRegistryPostProcessorBuilder(String... basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * Use a different bean name generator.
     *
     * @see BeanDefinitionReaderUtils#generateBeanName(BeanDefinition, BeanDefinitionRegistry)
     */
    public LegacyBeanRegistryPostProcessorBuilder beanNaming(BeanNameGenerator beanNameGenerator) {
        this.beanNameGenerator = beanNameGenerator;
        return this;
    }

    /**
     * Adjust the order of the post processor.
     *
     * @see org.springframework.core.PriorityOrdered
     */
    public LegacyBeanRegistryPostProcessorBuilder ordered(int order) {
        this.order = order;
        return this;
    }

    /**
     * Start to configure singleton bean registration.
     */
    public SingletonBuilder singletonsFrom() {
        return new SingletonBuilder();
    }

    /**
     * Start to configure prototype bean registration.
     */
    public PrototypeBuilder prototypesFrom() {
        return new PrototypeBuilder();
    }

    public FactoryBuilder factory(String type) {
        return new FactoryBuilder(() -> CustomizingTypeFilter.getType(type)
                .orElseThrow(() -> new FatalBeanException("Cannot inspect bean type " + type)));
    }

    public FactoryBuilder factory(Class<?> type) {
        return new FactoryBuilder(() -> type);
    }

    /**
     * Create the post processor as configured by the builder.
     * If neither {@link #singletonsFrom()} nor {@link #prototypesFrom()} has been called
     * any static methods and fields will be added as singletons.
     *
     * @see org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
     */
    public LegacyBeanRegistryPostProcessor build() {
        if (included.isEmpty()) {
            included.add(new LegacyBeanMethodFilter(SCOPE_SINGLETON, anyGetter()));
            included.add(new LegacySingletonFieldFilter(anyConstant()));
        }
        return new LegacyBeanRegistryPostProcessor(included, factories, beanNameGenerator, order, basePackages);
    }

    private abstract class Builder {

        LegacyBeanRegistryPostProcessorBuilder addIncluded(CustomizingTypeFilter<?> filter) {
            LegacyBeanRegistryPostProcessorBuilder.this.included.add(filter);
            return LegacyBeanRegistryPostProcessorBuilder.this;
        }
    }

    public class SingletonBuilder extends Builder {

        /**
         * Register singleton beans from static fields.
         * @param fieldCheck the additional check fields have to fulfill to be included
         */
        public LegacyBeanRegistryPostProcessorBuilder fields(Predicate<? super Field> fieldCheck) {
            return addIncluded(new LegacySingletonFieldFilter(fieldCheck));
        }

        /**
         * Register singleton beans from static methods.
         * @param memberCheck the additional check methods have to fulfill to be included
         */
        public LegacyBeanRegistryPostProcessorBuilder methods(Predicate<? super Method> memberCheck) {
            return addIncluded(new LegacyBeanMethodFilter(SCOPE_SINGLETON, memberCheck));
        }
    }

    public class PrototypeBuilder extends Builder {

        /**
         * Register prototypes beans from static methods.
         * @param memberCheck the additional check methods have to fulfill to be included
         */
        public LegacyBeanRegistryPostProcessorBuilder methods(Predicate<? super Method> memberCheck) {
            return addIncluded(new LegacyBeanMethodFilter(SCOPE_PROTOTYPE, memberCheck));
        }
    }

    public class FactoryBuilder {

        private final Supplier<Class<?>> type;

        private FactoryBuilder(Supplier<Class<?>> type) {
            this.type = type;
        }

        public LegacyBeanRegistryPostProcessorBuilder singletons(Predicate<? super Method> methodCheck) {
            return addFactory(new LegacyFactoryBeanScanner(type, methodCheck, SCOPE_SINGLETON));
        }

        public LegacyBeanRegistryPostProcessorBuilder prototypes(Predicate<? super Method> methodCheck) {
            return addFactory(new LegacyFactoryBeanScanner(type, methodCheck, SCOPE_PROTOTYPE));
        }

        private LegacyBeanRegistryPostProcessorBuilder addFactory(Function<BeanDefinitionRegistry, Stream<BeanDefinition>> factory) {
            factories.add(factory);
            return LegacyBeanRegistryPostProcessorBuilder.this;
        }
    }

    public static Predicate<Member> all() {
        return member -> true;
    }

    public static Predicate<Member> named(String... names) {
        return member -> asList(names).contains(member.getName());
    }

    public static Predicate<Member> named(Pattern name) {
        return member -> name.matcher(member.getName()).matches();
    }

    public static Predicate<Method> anyGetter() {
        return field -> GETTERS.matcher(field.getName()).matches();
    }

    public static Predicate<Field> anyConstant() {
        return field -> CONSTANTS.matcher(field.getName()).matches();
    }
}
