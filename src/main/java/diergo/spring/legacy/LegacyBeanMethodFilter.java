package diergo.spring.legacy;

import static diergo.spring.legacy.MemberPredicates.returning;
import static diergo.spring.legacy.MemberPredicates.withoutParameters;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.factory.config.BeanDefinition;

class LegacyBeanMethodFilter extends CustomizingTypeFilter<Method> {

    private final String scope;

    LegacyBeanMethodFilter(String scope, Predicate<? super Method> accessCheck) {
        super(withoutParameters().and(accessCheck));
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
