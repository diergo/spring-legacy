package example.spring;

import diergo.spring.legacy.LegacyBeanRegistryPostProcessor;
import diergo.spring.legacy.LegacySpringAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.regex.Pattern;

import static diergo.spring.legacy.LegacyBeanRegistryPostProcessorBuilder.legacyPackages;
import static diergo.spring.legacy.LegacyBeanRegistryPostProcessorBuilder.named;

@Configuration
@ComponentScan
@Import(LegacySpringAccess.class)
public class SpringConfig {

    @Bean
    static LegacyBeanRegistryPostProcessor legacySingletons() {
        return legacyPackages("example")
                .singletonsFrom().fields(named("INSTANCE"))
                .singletonsFrom().methods(named("getInstance"))
                .prototypesFrom().methods(named(Pattern.compile("create[A-Z].*")))
                .build();
    }
}
