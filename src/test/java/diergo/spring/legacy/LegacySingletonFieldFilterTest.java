package diergo.spring.legacy;

import example.legacy.LegacySingletonByField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public class LegacySingletonFieldFilterTest {

    private LegacySingletonFieldFilter tested = new LegacySingletonFieldFilter();

    @Test
    public void singletonWithStaticFieldMatches() {
        assertThat(matchTypeFilter(LegacySingletonByField.class), is(true));
        assertThat(matchBeanDefinition(LegacySingletonByField.class), is(true));
    }

    @Test
    public void beanDefinitionOfSingletonWithStaticFieldGetAnInstanceSupplierOnCustomize() {
        RootBeanDefinition actual = new RootBeanDefinition(LegacySingletonByField.class);

        tested.customize(actual);

        assertThat(actual.getInstanceSupplier().get(), isA(LegacySingletonByField.class));
    }

    @Test
    public void missingStaticFieldDoesNotMatch() {
        assertThat(matchTypeFilter(NonSingletonBean.class), is(false));
        assertThat(matchBeanDefinition(NonSingletonBean.class), is(false));
    }

    private boolean matchTypeFilter(Class<?> type) {
        return tested.match(new TestMetadataReader(type), null);
    }

    private boolean matchBeanDefinition(Class<?> type) {
        return tested.match(new RootBeanDefinition(type));
    }
}