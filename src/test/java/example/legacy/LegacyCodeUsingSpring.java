package example.legacy;

import example.spring.IndependentSpringBean;
import org.springframework.beans.FatalBeanException;

import static diergo.spring.legacy.LegacySpringAccess.getSpringBean;

/**
 * A simple class using code which became a Spring bean.
 */
public class LegacyCodeUsingSpring {

    private final IndependentSpringBean springBean;

    /**
     * The old constructor without DI typically used by production code.
     */
    public LegacyCodeUsingSpring() {
        this(getSpringBean(IndependentSpringBean.class));
    }

    /**
     * A new constructor with DI to be used by tests or from new code with DI.
     */
    LegacyCodeUsingSpring(IndependentSpringBean springBean) {
        this.springBean = springBean;
    }

    /**
     * Try to access a method of the Spring bean.
     * @return whether the call was successful or not
     */
    public boolean canUseSpring() {
        try {
            springBean.doIt();
            return true;
        } catch (FatalBeanException error) {
            return false;
        }
    }
}
