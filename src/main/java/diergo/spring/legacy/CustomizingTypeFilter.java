package diergo.spring.legacy;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Optional;

/**
 * Support class to combine type filtering, bean definition filtering and customizing as needed by the post processor.
 * @see LegacySingletonsRegistryPostProcessor
 */
abstract class CustomizingTypeFilter<T> implements TypeFilter, BeanDefinitionCustomizer {

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        return getType(metadataReader.getClassMetadata().getClassName())
                .flatMap(this::getSingletonAccess)
                .isPresent();
    }

    public boolean match(BeanDefinition bd) {
        return getType(bd.getBeanClassName())
                .flatMap(this::getSingletonAccess)
                .isPresent();
    }

    @Override
    public void customize(BeanDefinition bd) {
        getType(bd.getBeanClassName())
                .flatMap(this::getSingletonAccess)
                .ifPresent(access -> customizeBeanDefinition(access, bd));
    }

    protected abstract Optional<T> getSingletonAccess(Class<?> type);

    protected abstract void customizeBeanDefinition(T access, BeanDefinition bd);

    private Optional<Class<?>> getType(String className) {
        try {
            return Optional.of(Class.forName(className, false, getClass().getClassLoader()));
        } catch (ClassNotFoundException | LinkageError e) {
            return Optional.empty();
        }
    }
}
