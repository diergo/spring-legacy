package diergo.spring.legacy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.StaticApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LegacySpringAccessTest {

    private BeanFactory beanFactory;

    @Test
    public void springBeanCanBeRetrievedAsProxyBeforeApplicationContextAvailable() {
        TestBean actual = LegacySpringAccess.getInstance(TestBean.class);

        assertThat(AopUtils.isAopProxy(actual), is(true));
    }

    @Test
    public void springBeanProxyCannotBeUsedBeforeApplicationContextAvailable() {
        TestBean actual = LegacySpringAccess.getInstance(TestBean.class);

        assertThrows(FatalBeanException.class, actual::doIt);
    }

    @Test
    public void springBeanProxyCanBeUsedAfterApplicationContextAppeared() {
        TestBean actual = LegacySpringAccess.getInstance(TestBean.class);

        new LegacySpringAccess().setBeanFactory(beanFactory);

        actual.doIt();
    }

    @Test
    public void springBeanIsRetrievedFromApplicationContextIfAvailable() {
        new LegacySpringAccess().setBeanFactory(beanFactory);

        TestBean actual = LegacySpringAccess.getInstance(TestBean.class);

        assertThat(AopUtils.isAopProxy(actual), is(false));
    }

    @BeforeEach
    void createSpringContextWithTestBean() {
        StaticApplicationContext inner = new StaticApplicationContext();
        inner.registerSingleton("testBean", TestBean.class);
        beanFactory = inner.getBeanFactory();
    }

    @AfterEach
    void cleanupContext() {
        new LegacySpringAccess().destroy();
    }

    static class TestBean {

        public void doIt() {
        }
    }
}
