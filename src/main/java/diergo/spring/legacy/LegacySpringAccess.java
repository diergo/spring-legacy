package diergo.spring.legacy;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Configuration;

/**
 * Support access to Spring beans from outside an application context.
 * Import this configuration from one of your configuration classes before using it.
 *
 * @see org.springframework.context.annotation.Import
 */
@Configuration
public class LegacySpringAccess implements BeanFactoryAware, DisposableBean {

    private static final AtomicReference<BeanFactory> BEAN_FACTORY_HOLDER = new AtomicReference<>();

    /**
     * Get unique bean from Spring factory.
     * This is a way to access Spring beans from legacy code where you cannot use DI.
     * Calling before context started will return a proxy delegating to the context if available.
     * Do not use such proxies from constructor code!
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> type) {
        BeanFactory context = BEAN_FACTORY_HOLDER.get();
        if (context == null) {
            return (T) ProxyFactory.getProxy(new DelegatingTargetSource<>(type));
        }
        return context.getBean(type);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        BEAN_FACTORY_HOLDER.compareAndSet(null, beanFactory);
    }

    @Override
    public void destroy() {
        LegacySpringAccess.BEAN_FACTORY_HOLDER.set(null);
    }

    private static class DelegatingTargetSource<T> implements TargetSource {

        private final Class<T> type;
        private T target;

        private DelegatingTargetSource(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<?> getTargetClass() {
            return type;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public T getTarget() {
            if (target == null) {
                target = getFromContext();
            }
            return target;
        }

        @Override
        public void releaseTarget(Object target) {

        }

        private T getFromContext() {
            return BEAN_FACTORY_HOLDER.updateAndGet(old -> {
                if (old != null) {
                    return old;
                }
                throw new ApplicationContextException("Spring application context not started");
            }).getBean(type);
        }
    }
}
