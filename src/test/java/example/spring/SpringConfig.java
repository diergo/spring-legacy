package example.spring;

import diergo.spring.legacy.LegacySingletonsRegistryPostProcessor;
import diergo.spring.legacy.LegacySpringAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static diergo.spring.legacy.LegacySingletonsRegistryPostProcessor.scanForSingletons;

@Configuration
@ComponentScan
@Import(LegacySpringAccess.class)
public class SpringConfig {

    @Bean
    static LegacySingletonsRegistryPostProcessor legacySingletons() {
        return scanForSingletons("example")
                .asPostProcessor();
    }
}
