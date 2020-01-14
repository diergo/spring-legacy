package diergo.spring.legacy;

import example.legacy.LegacyPrototypeByStaticMethod;
import example.legacy.LegacySingletonByMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

public class LegacyBeanMethodFilterTest {

    private static final MetadataReaderFactory EXAMPLE_FACTORY = new SimpleMetadataReaderFactory();

    @Test
    public void singletonWithAnyStaticMethodMatches() {
        LegacyBeanMethodFilter tested = new LegacyBeanMethodFilter(false);
        assertThat(matchTypeFilter(LegacySingletonByMethod.class, tested), is(true));
        assertThat(matchBeanDefinition(LegacySingletonByMethod.class, tested), is(true));
    }

    @Test
    public void prototypeWithAnyStaticMethodMatches() {
        LegacyBeanMethodFilter tested = new LegacyBeanMethodFilter(true);
        assertThat(matchTypeFilter(LegacyPrototypeByStaticMethod.class, tested), is(true));
        assertThat(matchBeanDefinition(LegacyPrototypeByStaticMethod.class, tested), is(true));
    }

    @Test
    public void prototypeWithNamedStaticMethodMatches() {
        assertThat(matchTypeFilter(LegacyPrototypeByStaticMethod.class, new LegacyBeanMethodFilter(true,
                "createInstance")), is(true));
        assertThat(matchTypeFilter(LegacyPrototypeByStaticMethod.class, new LegacyBeanMethodFilter(true,
                "getInstance")), is(false));
    }

    @Test
    public void prototypeWithRegExpStaticMethodMatches() {
        assertThat(matchTypeFilter(LegacyPrototypeByStaticMethod.class, new LegacyBeanMethodFilter(true,
                Pattern.compile("[a-z]+Instance"))), is(true));
        assertThat(matchTypeFilter(LegacyPrototypeByStaticMethod.class, new LegacyBeanMethodFilter(true,
                Pattern.compile("get.*"))), is(false));
    }

    @Test
    public void beanDefinitionOfSingletonWithStaticMethodWillGetTheFactoryMethodOnCustomize() {
        RootBeanDefinition actual = new RootBeanDefinition(LegacySingletonByMethod.class);

        new LegacyBeanMethodFilter(false).customize(actual);

        assertThat(actual.getScope(), is(SCOPE_SINGLETON));
        assertThat(actual.isLazyInit(), is(true));
        assertThat(actual.getFactoryMethodName(), is("getInstance"));
    }

    @Test
    public void beanDefinitionOfPrototypeWithStaticMethodWillGetTheFactoryMethodOnCustomize() {
        RootBeanDefinition actual = new RootBeanDefinition(LegacySingletonByMethod.class);

        new LegacyBeanMethodFilter(true).customize(actual);

        assertThat(actual.getScope(), is(SCOPE_PROTOTYPE));
        assertThat(actual.isLazyInit(), is(false));
        assertThat(actual.getFactoryMethodName(), is("getInstance"));
    }

    @Test
    public void missingStaticMethodDoesNotMatch() {
        LegacyBeanMethodFilter tested = new LegacyBeanMethodFilter(false);
        assertThat(matchTypeFilter(NonSingletonBean.class, tested), is(false));
        assertThat(matchBeanDefinition(NonSingletonBean.class, tested), is(false));
    }

    private boolean matchTypeFilter(Class<?> type, CustomizingTypeFilter<?> filter) {
        return filter.match(new TestMetadataReader(type), EXAMPLE_FACTORY);
    }

    private boolean matchBeanDefinition(Class<?> type, CustomizingTypeFilter<?> filter) {
        return filter.match(new RootBeanDefinition(type));
    }
}
