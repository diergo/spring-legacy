package diergo.spring.legacy;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class MemberPredicates {

    private static final Pattern GETTERS = Pattern.compile("get[A-Z].+");
    private static final Pattern CONSTANTS = Pattern.compile("[A-Z][A-Z0-9_]+");

    public static <T extends Member> Predicate<T> all() {
        return member -> true;
    }

    public static <T extends Member> Predicate<T> atClass() {
        return member -> Modifier.isStatic(member.getModifiers());
    }

    public static <T extends Member> Predicate<T> atInstance() {
        return member -> !Modifier.isStatic(member.getModifiers());
    }

    public static <T extends Member> Predicate<T> visible() {
        return member -> !Modifier.isPrivate(member.getModifiers());
    }

    public static <T extends Member> Predicate<T> named(String... names) {
        return member -> asList(names).contains(member.getName());
    }

    public static <T extends Member> Predicate<T> named(Pattern name) {
        return member -> name.matcher(member.getName()).matches();
    }

    public static Predicate<Field> withType(Class<?> type) {
        return field -> type.isAssignableFrom(field.getType());
    }

    public static Predicate<Method> returning(Class<?> returnType) {
        return method -> returnType.isAssignableFrom(method.getReturnType());
    }

    public static Predicate<Method> returningBeanType() {
        return method -> method.getReturnType() != Void.class
                && !method.getReturnType().isPrimitive()
                && !method.getReturnType().isArray();
    }

    public static Predicate<Method> withoutParameters() {
        return method -> method.getParameterCount() == 0;
    }

    public static Predicate<Method> noObjectMethod() {
        return method -> method.getDeclaringClass() != Object.class;
    }

    public static Predicate<Method> anyGetter() {
        return named(GETTERS);
    }

    public static Predicate<Field> anyConstant() {
        return named(CONSTANTS);
    }
}
