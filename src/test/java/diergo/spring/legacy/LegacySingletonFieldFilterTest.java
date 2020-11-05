package diergo.spring.legacy;

import example.legacy.LegacySingletonByField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

public class LegacySingletonFieldFilterTest {

    private static final MetadataReaderFactory EXAMPLE_FACTORY = new SimpleMetadataReaderFactory();

    @Test
    public void singletonWithAnyStaticFieldMatches() {
        assertThat(matchTypeFilter(LegacySingletonByField.class, new LegacySingletonFieldFilter(field -> true)), is(true));
        assertThat(matchBeanDefinition(LegacySingletonByField.class, new LegacySingletonFieldFilter(field -> true)), is(true));
    }

    @Test
    public void rootBeanDefinitionOfSingletonWithStaticFieldGetAnInstanceSupplierOnCustomize() {
        RootBeanDefinition actual = new RootBeanDefinition(LegacySingletonByField.class);

        new LegacySingletonFieldFilter(field -> true).customize(actual);

        assertThat(actual.getScope(), is(SCOPE_SINGLETON));
        assertThat(actual.isLazyInit(), is(true));
        assertThat(actual.getInstanceSupplier().get(), isA(LegacySingletonByField.class));
    }

    @Test
    public void scannedBbeanDefinitionOfSingletonWithStaticFieldGetAnInstanceSupplierOnCustomize() {
        ScannedGenericBeanDefinition actual = new ScannedGenericBeanDefinition(new TestMetadataReader(LegacySingletonByField.class));

        new LegacySingletonFieldFilter(field -> true).customize(actual);

        assertThat(actual.getScope(), is(SCOPE_SINGLETON));
        assertThat(actual.isLazyInit(), is(true));
        assertThat(actual.getInstanceSupplier().get(), isA(LegacySingletonByField.class));
    }

    @Test
    public void prototypeWithSuccessfulCheckMatches() {
        assertThat(matchTypeFilter(LegacySingletonByField.class,
                new LegacySingletonFieldFilter(field -> field.getName().equals("INSTANCE"))),
                is(true));
        assertThat(matchTypeFilter(LegacySingletonByField.class,
                new LegacySingletonFieldFilter(field -> field.getName().equals("instance"))),
                is(false));
    }

    @Test
    public void missingStaticFieldDoesNotMatch() {
        assertThat(matchTypeFilter(NonSingletonBean.class, new LegacySingletonFieldFilter(field -> true)),
                is(false));
        assertThat(matchBeanDefinition(NonSingletonBean.class, new LegacySingletonFieldFilter(field -> true)),
                is(false));
    }

    private boolean matchTypeFilter(Class<?> type, CustomizingTypeFilter<?> filter) {
        return filter.match(new TestMetadataReader(type), EXAMPLE_FACTORY);
    }

    private boolean matchBeanDefinition(Class<?> type, CustomizingTypeFilter<?> filter) {
        return filter.supports(new RootBeanDefinition(type));
    }
}