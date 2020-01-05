package example.spring;

import example.legacy.LegacySingletonByMethod;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanInjectedLegacy {

    private final LegacySingletonByMethod legacyDependency;

    public SpringBeanInjectedLegacy(LegacySingletonByMethod legacyDependency) {
        this.legacyDependency = legacyDependency;
    }
}
