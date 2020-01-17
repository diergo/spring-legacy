package diergo.spring.legacy;

import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

class LegacyBeanMethodFilter extends CustomizingTypeFilter<Method> {

    private final boolean prototype;

    LegacyBeanMethodFilter(boolean prototype, String... methodNames) {
        super(methodNames);
        this.prototype = prototype;
    }

    LegacyBeanMethodFilter(boolean prototype, Pattern methodNamePattern) {
        super(methodNamePattern);
        this.prototype = prototype;
    }

    @Override
    protected Optional<Method> getAccess(Class<?> type) {
        return Stream.of(type.getDeclaredMethods())
                .filter(method -> isStaticSingletonMethod(method, type))
                .filter(method -> nameMatch(method.getName()))
                .findFirst();
    }

    @Override
    protected void customizeBeanDefinition(Method access, BeanDefinition bd) {
        if (prototype) {
            bd.setScope(SCOPE_PROTOTYPE);
        } else {
            bd.setScope(SCOPE_SINGLETON);
            bd.setLazyInit(true);
        }
        bd.setFactoryMethodName(access.getName());
    }

    private static boolean isStaticSingletonMethod(Method method, Class<?> returnType) {
        return isStatic(method.getModifiers()) && !isPrivate(method.getModifiers())
                && method.getParameterCount() == 0 && returnType.isAssignableFrom(method.getReturnType());
    }
}
