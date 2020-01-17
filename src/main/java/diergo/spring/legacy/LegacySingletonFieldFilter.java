package diergo.spring.legacy;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

class LegacySingletonFieldFilter extends CustomizingTypeFilter<Field> {

    LegacySingletonFieldFilter(String... fieldNames) {
        super(fieldNames);
    }

    LegacySingletonFieldFilter(Pattern fieldNamePattern) {
        super(fieldNamePattern);
    }

    @Override
    protected Optional<Field> getAccess(Class<?> type) {
        return Stream.of(type.getDeclaredFields())
                .filter(field -> isStaticSingletonField(field, type))
                .filter(field -> nameMatch(field.getName()))
                .findFirst();
    }

    @Override
    protected void customizeBeanDefinition(Field access, BeanDefinition bd) {
        bd.setScope(SCOPE_SINGLETON);
        bd.setLazyInit(true);
        if (bd instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition adb = (AbstractBeanDefinition) bd;
            adb.setInstanceSupplier(() -> {
                access.setAccessible(true);
                try {
                    return access.get(adb.hasBeanClass() ? adb.getBeanClass() : Class.forName(adb.getBeanClassName()));
                } catch (IllegalAccessException | ClassNotFoundException e) {
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
