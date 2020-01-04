package example.spring;

import diergo.spring.legacy.LegacySpringAccess;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import(LegacySpringAccess.class)
public class SpringConfig {
}
