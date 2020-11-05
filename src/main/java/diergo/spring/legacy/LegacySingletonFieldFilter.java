package diergo.spring.legacy;

import static diergo.spring.legacy.MemberPredicates.withBeanType;
import static diergo.spring.legacy.MemberPredicates.withType;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * A type filter creating bean definitions for fields with a valid bean type.
 *
 * @see MemberPredicates#withBeanType()
 * @see LegacyBeanRegistryPostProcessorBuilder.SingletonBuilder#fields(Predicate)
 */
class LegacySingletonFieldFilter extends CustomizingTypeFilter<Field> {

    LegacySingletonFieldFilter(Predicate<? super Field> accessCheck) {
        super(withBeanType().and(accessCheck));
    }

    @Override
    public boolean supports(BeanDefinition bd) {
        return super.supports(bd) && bd instanceof AbstractBeanDefinition;
    }

    @Override
    protected Optional<Field> getAccess(Class<?> type) {
        return Stream.of(type.getDeclaredFields())
                .filter(withType(type).and(accessCheck))
                .findFirst();
    }

    @Override
    protected void customizeBeanDefinition(Field access, BeanDefinition bd) {
        bd.setScope(SCOPE_SINGLETON);
        bd.setLazyInit(true);
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
