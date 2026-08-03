package co.casterlabs.caffeinated.window.jcef;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import app.saucer.bridge.Mutable;
import co.casterlabs.rakurai.json.TypeToken;

class _Reflection {

    /**
     * This loops through the inheritance to look for private fields.
     */
    static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new LinkedList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * This loops through the inheritance to look for private methods.
     */
    static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new LinkedList<>();
        while (clazz != null) {
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    static TypeToken<?> realType(Object obj, Field f) throws IllegalArgumentException, IllegalAccessException {
        if (f.getType() == Mutable.class) {
            Mutable<?> mut = (Mutable<?>) f.get(obj);
            return mut.type;
        } else {
            return TypeToken.of(f.getType());
        }
    }

    static TypeToken<?> inputType(Method m) throws IllegalArgumentException, IllegalAccessException {
        Class<?> type = m.getParameterTypes()[0];
        return TypeToken.of(type);
    }

}
