package diergo.spring.legacy;

import static org.springframework.util.ReflectionUtils.getAllDeclaredMethods;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class LegacyFactoryBeanScanner implements Function<BeanDefinitionRegistry, Stream<BeanDefinition>> {

    private final Supplier<Class<?>> type;
    private final Predicate<? super Method> methodCheck;
    private final String scope;

    public LegacyFactoryBeanScanner(Supplier<Class<?>> type, Predicate<? super Method> methodCheck, String scope) {
        this.type = type;
        this.methodCheck = MemberPredicates.withoutParameters()
                .and(MemberPredicates.returningBeanType())
                .and(MemberPredicates.noObjectMethod())
                .and(MemberPredicates.visible())
                .and(MemberPredicates.atInstance())
                .and(methodCheck);
        this.scope = scope;
    }

    @Override
    public Stream<BeanDefinition> apply(BeanDefinitionRegistry registry) {
        Class<?> clazz = type.get();
        String factoryBean = findFactoryBean(clazz, registry);
        return Stream.of(getAllDeclaredMethods(clazz))
                .filter(methodCheck)
                .map(method -> createBeanDefinition(factoryBean, method));
    }

    private String findFactoryBean(Class<?> type, BeanDefinitionRegistry registry) {
        return Stream.of(registry.getBeanDefinitionNames())
                .map(name -> new BeanDefinitionHolder(registry.getBeanDefinition(name), name))
                .filter(bdh -> type.getName().equals(bdh.getBeanDefinition().getBeanClassName()))
                .map(BeanDefinitionHolder::getBeanName)
                .findAny()
                .orElseThrow(() -> new FatalBeanException("Missing factory bean of type " + type));
    }

    private GenericBeanDefinition createBeanDefinition(String factoryBean, Method method) {
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setFactoryBeanName(factoryBean);
        bd.setFactoryMethodName(method.getName());
        bd.setBeanClass(method.getReturnType());
        bd.setScope(scope);
        bd.setDependsOn(factoryBean);
        return bd;
    }
}
