package diergo.spring.legacy;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Predicates to filter methods and fields.
 *
 * @see LegacyBeanRegistryPostProcessorBuilder
 */
public final class MemberPredicates {

    private static final Pattern GETTERS = Pattern.compile("get[A-Z].+");
    private static final Pattern CONSTANTS = Pattern.compile("[A-Z][A-Z0-9_]+");

    /**
     * Allow any members.
     */
    public static <T extends Member> Predicate<T> all() {
        return member -> true;
    }

    /**
     * Allow members declared on type level aka static.
     */
    public static <T extends Member> Predicate<T> atClass() {
        return member -> Modifier.isStatic(member.getModifiers());
    }

    /**
     * Allow members declared on instance level aka non static.
     */
    public static <T extends Member> Predicate<T> atInstance() {
        return member -> !Modifier.isStatic(member.getModifiers());
    }

    /**
     * Allow members declared visible aka non private.
     */
    public static <T extends Member> Predicate<T> visible() {
        return member -> !Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Allow members having any of the passed names.
     */
    public static <T extends Member> Predicate<T> named(String... names) {
        return member -> asList(names).contains(member.getName());
    }

    /**
     * Allow members having a name matching the pattern.
     */
    public static <T extends Member> Predicate<T> named(Pattern name) {
        return member -> name.matcher(member.getName()).matches();
    }

    /**
     * Allow fields with the specified type or a subtype.
     */
    public static Predicate<Field> withType(Class<?> type) {
        return field -> type.isAssignableFrom(field.getType());
    }

    /**
     * Allow fields with any valid bean type. This excludes primitive types and arrays.
     */
    public static Predicate<Field> withBeanType() {
        return field -> field.getType() != Void.class
                && !field.getType().isPrimitive()
                && !field.getType().isArray();
    }

    /**
     * Allow methods returning the specified type or a subtype.
     */
    public static Predicate<Method> returning(Class<?> returnType) {
        return method -> returnType.isAssignableFrom(method.getReturnType());
    }

    /**
     * Allow methods returning any valid bean type. This excludes primitive types and arrays.
     */
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
