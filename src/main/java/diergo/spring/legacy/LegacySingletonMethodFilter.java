package diergo.spring.legacy;

import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;

class LegacySingletonMethodFilter extends CustomizingTypeFilter<Method> {

    private final String[] methodNames;

    LegacySingletonMethodFilter(String... methodNames) {
        this.methodNames = methodNames;
    }

    @Override
    protected Optional<Method> getSingletonAccess(Class<?> type) {
        return Stream.of(type.getDeclaredMethods())
                .filter(method -> isStaticSingletonMethod(method, type))
                .filter(method -> methodNames.length == 0 || Arrays.asList(methodNames).contains(method.getName()))
                .findFirst();
    }

    @Override
    protected void customizeBeanDefinition(Method access, BeanDefinition bd) {
        bd.setFactoryMethodName(access.getName());
    }

    private static boolean isStaticSingletonMethod(Method method, Class<?> returnType) {
        return isStatic(method.getModifiers()) && !isPrivate(method.getModifiers())
                && method.getParameterCount() == 0 && returnType.isAssignableFrom(method.getReturnType());
    }
}
