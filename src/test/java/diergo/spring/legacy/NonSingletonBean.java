package diergo.spring.legacy;

import java.util.Properties;

class NonSingletonBean {

    private static final Properties props = new Properties();

    // static getter of Map type is ignored
    public static Properties getProps() {
        return props;
    }
}
