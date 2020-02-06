package diergo.spring.legacy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

public class LegacyFactoryBeanScanner implements Function<BeanDefinitionRegistry, Stream<BeanDefinition>> {

    private final Supplier<Class<?>> type;
    private final Predicate<? super Method> methodCheck;
    private final String scope;

    public LegacyFactoryBeanScanner(Supplier<Class<?>> type, Predicate<? super Method> methodCheck, String scope) {
        this.type = type;
        this.methodCheck = methodCheck;
        this.scope = scope;
    }

    @Override
    public Stream<BeanDefinition> apply(BeanDefinitionRegistry registry) {
        Class<?> clazz = type.get();
        String factoryBean = findFactoryBean(clazz, registry);
        return Stream.of(clazz.getDeclaredMethods())
                .filter(LegacyFactoryBeanScanner::isFactoryMethod)
                .filter(methodCheck)
                .map(method -> {
                    GenericBeanDefinition bd = new GenericBeanDefinition();
                    bd.setBeanClass(LegacyFactoryBean.class);
                    ConstructorArgumentValues argumentValues = new ConstructorArgumentValues();
                    argumentValues.addIndexedArgumentValue(0, factoryBean);
                    argumentValues.addIndexedArgumentValue(1, method);
                    argumentValues.addIndexedArgumentValue(2, SCOPE_SINGLETON.equals(scope));
                    bd.setConstructorArgumentValues(argumentValues);
                    bd.setDependsOn(factoryBean);
                    return bd;
                });
    }

    private String findFactoryBean(Class<?> type, BeanDefinitionRegistry registry) {
        return Stream.of(registry.getBeanDefinitionNames())
                .map(name -> new BeanDefinitionHolder(registry.getBeanDefinition(name), name))
                .filter(bdh -> type.getName().equals(bdh.getBeanDefinition().getBeanClassName()))
                .map(BeanDefinitionHolder::getBeanName)
                .findAny()
                .orElse(null);
    }

    private static boolean isFactoryMethod(Method method) {
        return !isStatic(method.getModifiers()) && !isPrivate(method.getModifiers())
                && method.getParameterCount() == 0;
    }

    static class LegacyFactoryBean implements FactoryBean<Object>, BeanFactoryAware {

        private final String factoryBean;
        private final Method method;
        private final boolean singleton;
        private BeanFactory beanFactory;

        LegacyFactoryBean(String factoryBean, Method method, boolean singleton) {
            this.factoryBean = factoryBean;
            this.method = method;
            this.singleton = singleton;
        }

        @Override
        public Object getObject() throws Exception {
            Object factory = beanFactory.getBean(factoryBean);
            method.setAccessible(true);
            return method.invoke(factory);
        }

        @Override
        public Class<?> getObjectType() {
            return method.getReturnType();
        }

        @Override
        public boolean isSingleton() {
            return singleton;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }
    }
}
