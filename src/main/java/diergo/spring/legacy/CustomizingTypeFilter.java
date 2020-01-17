package diergo.spring.legacy;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Support class to combine type filtering, bean definition filtering and customizing as needed by the post processor.
 * @see LegacyBeanRegistryPostProcessor
 */
abstract class CustomizingTypeFilter<T> implements TypeFilter, BeanDefinitionCustomizer {

    private final Pattern namePattern;
    private final String[] names;

    CustomizingTypeFilter(String... names) {
        this.names = names;
        namePattern = null;
    }

    CustomizingTypeFilter(Pattern namePattern) {
        this.names = new String[0];
        this.namePattern = namePattern;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        return getType(metadataReader.getClassMetadata().getClassName())
                .flatMap(this::getAccess)
                .isPresent();
    }

    public boolean match(BeanDefinition bd) {
        return getType(bd.getBeanClassName())
                .flatMap(this::getAccess)
                .isPresent();
    }

    @Override
    public void customize(BeanDefinition bd) {
        getType(bd.getBeanClassName())
                .flatMap(this::getAccess)
                .ifPresent(access -> customizeBeanDefinition(access, bd));
    }

    protected abstract Optional<T> getAccess(Class<?> type);

    protected abstract void customizeBeanDefinition(T access, BeanDefinition bd);

    protected boolean nameMatch(String name) {
        return (names.length == 0 || Arrays.asList(names).contains(name)) &&
                (namePattern == null || namePattern.matcher(name).matches());
    }

    static Optional<Class<?>> getType(String className) {
        try {
            return Optional.of(Class.forName(className, false, CustomizingTypeFilter.class.getClassLoader()));
        } catch (ClassNotFoundException | LinkageError e) {
            return Optional.empty();
        }
    }
}
