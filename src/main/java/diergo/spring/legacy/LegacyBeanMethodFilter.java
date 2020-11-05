package diergo.spring.legacy;

import static diergo.spring.legacy.MemberPredicates.noObjectMethod;
import static diergo.spring.legacy.MemberPredicates.returning;
import static diergo.spring.legacy.MemberPredicates.returningBeanType;
import static diergo.spring.legacy.MemberPredicates.withoutParameters;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * A type filter creating bean definitions for methods without parameters returning a valid bean type.
 *
 * @see MemberPredicates#withoutParameters()
 * @see MemberPredicates#returningBeanType()
 * @see LegacyBeanRegistryPostProcessorBuilder.SingletonBuilder#methods(Predicate)
 * @see LegacyBeanRegistryPostProcessorBuilder.PrototypeBuilder#methods(Predicate)
 */
class LegacyBeanMethodFilter extends CustomizingTypeFilter<Method> {

    private final String scope;

    LegacyBeanMethodFilter(String scope, Predicate<? super Method> accessCheck) {
        super(noObjectMethod()
                .and(withoutParameters())
                .and(returningBeanType())
                .and(accessCheck));
        this.scope = scope;
    }

    @Override
    protected Optional<Method> getAccess(Class<?> type) {
        return Stream.of(type.getDeclaredMethods())
                .filter(returning(type).and(accessCheck))
                .findFirst();
    }

    @Override
    protected void customizeBeanDefinition(Method access, BeanDefinition bd) {
        bd.setScope(scope);
        if (SCOPE_SINGLETON.equals(scope)) {
            bd.setLazyInit(true);
        }
        bd.setFactoryMethodName(access.getName());
    }
}
