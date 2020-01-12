package example.spring;

import diergo.spring.legacy.LegacyBeanRegistryPostProcessor;
import diergo.spring.legacy.LegacySpringAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static diergo.spring.legacy.LegacyBeanRegistryPostProcessor.scanForSingletons;

@Configuration
@ComponentScan
@Import(LegacySpringAccess.class)
public class SpringConfig {

    @Bean
    static LegacyBeanRegistryPostProcessor legacySingletons() {
        return scanForSingletons("example")
                .asPostProcessor();
    }
}
