package diergo.spring.legacy;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;

class LegacySingletonFieldFilter extends CustomizingTypeFilter<Field> {

    private final String[] fieldNames;

    LegacySingletonFieldFilter(String... fieldNames) {
        this.fieldNames = fieldNames;
    }

    @Override
    protected Optional<Field> getSingletonAccess(Class<?> type) {
        return Stream.of(type.getDeclaredFields())
                .filter(field -> isStaticSingletonField(field, type))
                .filter(field -> fieldNames.length == 0 || Arrays.asList(fieldNames).contains(field.getName()))
                .findFirst();
    }

    @Override
    protected void customizeBeanDefinition(Field access, BeanDefinition bd) {
        if (bd instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition adb = (AbstractBeanDefinition) bd;
            adb.setInstanceSupplier(() -> {
                access.setAccessible(true);
                try {
                    return access.get(adb.getBeanClass());
                } catch (IllegalAccessException e) {
                    throw new BeanCreationException("Cannot create bean using static singleton field " + access, e);
                }
            });
        }
    }

    private static boolean isStaticSingletonField(Field field, Class<?> returnType) {
        return isStatic(field.getModifiers()) && !isPrivate(field.getModifiers())
                && returnType.isAssignableFrom(field.getType());
    }
}
