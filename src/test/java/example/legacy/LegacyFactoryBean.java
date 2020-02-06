package example.legacy;

public class LegacyFactoryBean {

    private static final LegacyFactoryBean INSTANCE = new LegacyFactoryBean();

    public static LegacyFactoryBean getInstance() {
        return INSTANCE;
    }

    public CreatedSingleton getSingleton() {
        return new CreatedSingleton();
    }

    public CreatedPrototype createPrototype() {
        return new CreatedPrototype();
    }
}
