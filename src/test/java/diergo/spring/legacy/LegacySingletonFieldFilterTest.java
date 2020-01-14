package diergo.spring.legacy;

import example.legacy.LegacySingletonByField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

public class LegacySingletonFieldFilterTest {

    private static final MetadataReaderFactory EXAMPLE_FACTORY = new SimpleMetadataReaderFactory();

    @Test
    public void singletonWithAnyStaticFieldMatches() {
        assertThat(matchTypeFilter(LegacySingletonByField.class, new LegacySingletonFieldFilter()), is(true));
        assertThat(matchBeanDefinition(LegacySingletonByField.class, new LegacySingletonFieldFilter()), is(true));
    }

    @Test
    public void rootBeanDefinitionOfSingletonWithStaticFieldGetAnInstanceSupplierOnCustomize() {
        RootBeanDefinition actual = new RootBeanDefinition(LegacySingletonByField.class);

        new LegacySingletonFieldFilter().customize(actual);

        assertThat(actual.getScope(), is(SCOPE_SINGLETON));
        assertThat(actual.isLazyInit(), is(true));
        assertThat(actual.getInstanceSupplier().get(), isA(LegacySingletonByField.class));
    }

    @Test
    public void scannedBbeanDefinitionOfSingletonWithStaticFieldGetAnInstanceSupplierOnCustomize() {
        ScannedGenericBeanDefinition actual = new ScannedGenericBeanDefinition(new TestMetadataReader(LegacySingletonByField.class));

        new LegacySingletonFieldFilter().customize(actual);

        assertThat(actual.getScope(), is(SCOPE_SINGLETON));
        assertThat(actual.isLazyInit(), is(true));
        assertThat(actual.getInstanceSupplier().get(), isA(LegacySingletonByField.class));
    }

    @Test
    public void prototypeWithNamedStaticMethodMatches() {
        assertThat(matchTypeFilter(LegacySingletonByField.class, new LegacySingletonFieldFilter("INSTANCE")),
                is(true));
        assertThat(matchTypeFilter(LegacySingletonByField.class, new LegacySingletonFieldFilter("instance")),
                is(false));
    }

    @Test
    public void prototypeWithRegExpStaticMethodMatches() {
        assertThat(matchTypeFilter(LegacySingletonByField.class, new LegacySingletonFieldFilter(Pattern.compile("[A-Z_]+"))), is(true));
        assertThat(matchTypeFilter(LegacySingletonByField.class, new LegacySingletonFieldFilter(Pattern.compile("[a-z]+"))), is(false));
    }

    @Test
    public void missingStaticFieldDoesNotMatch() {
        assertThat(matchTypeFilter(NonSingletonBean.class, new LegacySingletonFieldFilter()), is(false));
        assertThat(matchBeanDefinition(NonSingletonBean.class, new LegacySingletonFieldFilter()), is(false));
    }

    private boolean matchTypeFilter(Class<?> type, CustomizingTypeFilter<?> filter) {
        return filter.match(new TestMetadataReader(type), EXAMPLE_FACTORY);
    }

    private boolean matchBeanDefinition(Class<?> type, CustomizingTypeFilter<?> filter) {
        return filter.match(new RootBeanDefinition(type));
    }
}