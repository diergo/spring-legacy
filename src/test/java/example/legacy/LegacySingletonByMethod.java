package example.legacy;

public class LegacySingletonByMethod {

    private static final LegacySingletonByMethod instance = new LegacySingletonByMethod();

    public static LegacySingletonByMethod getInstance() {
        return instance;
    }
}
