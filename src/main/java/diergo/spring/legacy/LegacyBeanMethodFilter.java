package diergo.spring.legacy;

import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

class LegacyBeanMethodFilter extends CustomizingTypeFilter<Method> {

    private final String scope;

    LegacyBeanMethodFilter(String scope, Predicate<? super Method> accessCheck) {
        super(accessCheck);
        this.scope = scope;
    }

    @Override
    protected Optional<Method> getAccess(Class<?> type) {
        return Stream.of(type.getDeclaredMethods())
                .filter(method -> isStaticSingletonMethod(method, type))
                .filter(accessCheck::test)
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

    private static boolean isStaticSingletonMethod(Method method, Class<?> returnType) {
        return isStatic(method.getModifiers()) && !isPrivate(method.getModifiers())
                && method.getParameterCount() == 0 && returnType.isAssignableFrom(method.getReturnType());
    }
}
