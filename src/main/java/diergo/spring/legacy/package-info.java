/**
 * The package supports dependency injection as provided by <a href="https://spring.io/projects/spring-framework">Spring</a>
 * framework also in legacy systems to make units more independent and testable.
 * To access Spring beans from legacy code, use {@link diergo.spring.legacy.LegacySpringAccess#getSpringBean(java.lang.Class)}.
 * To expose legacy code as Spring beans, use {@link diergo.spring.legacy.LegacyBeanRegistryPostProcessorBuilder} to configure
 * scan and bean creation for singletons, prototypes and factory beans.
 */
package diergo.spring.legacy;