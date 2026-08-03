package co.casterlabs.caffeinated.window.jcef;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.bridge.JavascriptValue;
import app.saucer.bridge.Mutable;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

class _JavascriptObjectWrapper {
    final String id = UUID.randomUUID().toString();
    final String path;

    private final MutableField[] mutableFields;
    private final Map<String, Supplier<?>> getters;
    private final Map<String, Consumer<JsonElement>> setters;
    private final Map<String, MethodWrapper> functions;

    _JavascriptObjectWrapper(String path, Class<?> objClass, Object obj) {
        this.path = path;

        List<MutableField> mutableFields = new ArrayList<>();
        Map<String, Supplier<?>> getters = new HashMap<>();
        Map<String, Consumer<JsonElement>> setters = new HashMap<>();
        Map<String, MethodWrapper> functions = new HashMap<>();

        // Register field getters/setters first. This is so method-based getters/setters
        // can override field-based ones.

        for (Field f : _Reflection.getAllFields(objClass)) {
            if (!f.isAnnotationPresent(JavascriptValue.class)) continue;

            JavascriptValue annotation = f.getDeclaredAnnotation(JavascriptValue.class);
            String name = annotation.value().isEmpty() ? f.getName() : annotation.value();

            if (annotation.allowGet()) {
                getters.put(name, new FieldGetter<>(obj, f));
            }
            if (annotation.allowSet()) {
                setters.put(name, new FieldSetter<>(obj, f));
            }
            if (annotation.watchForMutate()) {
                mutableFields.add(new MutableField(name, obj, f));
            }
        }

        for (Method m : _Reflection.getAllMethods(objClass)) {
            if (m.isAnnotationPresent(JavascriptGetter.class)) {
                JavascriptGetter annotation = m.getDeclaredAnnotation(JavascriptGetter.class);
                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();

                getters.put(name, new MethodGetter(obj, m));
            }

            if (m.isAnnotationPresent(JavascriptSetter.class)) {
                JavascriptSetter annotation = m.getDeclaredAnnotation(JavascriptSetter.class);
                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();

                setters.put(name, new MethodSetter(obj, m));
            }

            if (m.isAnnotationPresent(JavascriptFunction.class)) {
                JavascriptFunction annotation = m.getDeclaredAnnotation(JavascriptFunction.class);
                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();

                functions.put(name, new MethodWrapper(obj, m, annotation.ignoreReturn()));
            }
        }

        this.mutableFields = mutableFields.toArray(new MutableField[0]);
        this.getters = Collections.unmodifiableMap(getters);
        this.setters = Collections.unmodifiableMap(setters);
        this.functions = Collections.unmodifiableMap(functions);
    }

    List<String> properties() {
        List<String> properties = new ArrayList<>(this.getters.size() + this.setters.size());
        properties.addAll(this.getters.keySet());
        properties.addAll(this.setters.keySet());
        return properties;
    }

    List<String> functions() {
        return new ArrayList<>(this.functions.keySet());
    }

    @SneakyThrows
    @Nullable
    JsonElement handleGet(String field) {
        Supplier<?> getter = this.getters.get(field);
        if (getter == null) {
            return null; // undefined.
        }

        return Rson.DEFAULT.toJson(getter.get());
    }

    @SneakyThrows
    <T> void handleSet(String field, JsonElement newValue) {
        Consumer<JsonElement> setter = this.setters.get(field);
        if (setter == null) {
            throw new IllegalArgumentException("Cannot set field: " + field + ", did you mistype something or forget an annotation?");
        }

        setter.accept(newValue);
    }

    @SneakyThrows
    @Nullable
    JsonElement handleInvoke(String functionName, JsonArray parameters) {
        MethodWrapper wrapper = this.functions.get(functionName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Nonexistient function: " + functionName + ", did you mistype something or forget an annotation?");
        }

        return wrapper.invoke(parameters);
    }

    List<String> whichFieldsHaveMutated() {
        List<String> list = new LinkedList<>();
        for (MutableField field : this.mutableFields) {
            if (field.check()) {
                list.add(field.name);
            }
        }
        return list;
    }

    @RequiredArgsConstructor
    private static class MutableField {
        private final String name;
        private final @Nullable Object obj;
        private final Field f;

        private int lastHashCode = 0;
        private boolean isFirstCheck = true;

        @SneakyThrows
        private boolean check() {
            int currentHashCode = _Hashing.hash(this.f.get(this.obj));
            boolean has = this.isFirstCheck || this.lastHashCode != currentHashCode;

            this.isFirstCheck = false;
            this.lastHashCode = currentHashCode;

            return has;
        }

    }

    private static class MethodWrapper {
        private final @Nullable Object obj;
        private final Method m;

        private final TypeToken<?>[] parameterTypes;
        private final boolean noReturn;

        private MethodWrapper(Object obj, Method m, boolean noReturn) {
            this.obj = obj;
            this.m = m;

            this.m.setAccessible(true);

            Class<?>[] parameters = this.m.getParameterTypes();
            this.parameterTypes = new TypeToken<?>[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                this.parameterTypes[i] = TypeToken.of(parameters[i]);
            }

            this.noReturn = noReturn || this.m.getReturnType() == Void.class || this.m.getReturnType() == void.class;
        }

        @SneakyThrows
        private @Nullable JsonElement invoke(JsonArray parameters) {
            assert this.parameterTypes.length == parameters.size() : "The invoking arguments do not match the expected length: " + this.parameterTypes.length;

            Object[] args = new Object[this.parameterTypes.length];
            for (int i = 0; i < args.length; i++) {
                try {
                    args[i] = Rson.DEFAULT.fromJson(parameters.get(i), this.parameterTypes[i]);
                } catch (JsonParseException e) {
                    throw new IllegalArgumentException("The provided argument " + parameters.get(i) + " could not be converted to " + this.parameterTypes[i].getType().getTypeName());
                }
            }

            try {
                Object result = this.m.invoke(this.obj, args);

                if (this.noReturn) {
                    return null; // undefined (aka, void)
                } else {
                    return Rson.DEFAULT.toJson(result);
                }
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

    }

    /* -------------------------------- */
    /*      Getter Implementations      */
    /* -------------------------------- */

    private static class FieldGetter<T> implements Supplier<T> {
        private final @Nullable Object obj;
        private final Field f;

        private FieldGetter(Object obj, Field f) {
            this.obj = obj;
            this.f = f;

            this.f.setAccessible(true);
        }

        @SuppressWarnings("unchecked")
        @SneakyThrows
        @Override
        public T get() {
            T toReturn = (T) this.f.get(this.obj);

            if (toReturn instanceof Mutable) {
                return ((Mutable<T>) toReturn).get();
            } else {
                return toReturn;
            }
        }
    }

    private static class MethodGetter implements Supplier<Object> {
        private final @Nullable Object obj;
        private final Method m;

        private MethodGetter(Object obj, Method m) {
            this.obj = obj;
            this.m = m;

            this.m.setAccessible(true);
        }

        @SneakyThrows
        @Override
        public Object get() {
            return this.m.invoke(this.obj);
        }
    }

    /* -------------------------------- */
    /*      Setter Implementations      */
    /* -------------------------------- */

    @SuppressWarnings("unchecked")
    private static class FieldSetter<T> implements Consumer<JsonElement> {
        private final @Nullable Object obj;
        private final Field f;

        private final TypeToken<T> deserializationType;
        private final boolean isMutable;

        @SneakyThrows
        private FieldSetter(Object obj, Field f) {
            this.obj = obj;
            this.f = f;

            this.f.setAccessible(true);

            this.deserializationType = (TypeToken<T>) _Reflection.realType(obj, f);
            this.isMutable = f.getType() == Mutable.class;
        }

        @SneakyThrows
        @Override
        public void accept(JsonElement newValue) {
            T deserialized = Rson.DEFAULT.fromJson(newValue, this.deserializationType);

            if (this.isMutable) {
                Mutable<T> mut = (Mutable<T>) this.f.get(this.obj);
                mut.set(deserialized);
            } else {
                this.f.set(this.obj, newValue);
            }
        }
    }

    private static class MethodSetter implements Consumer<JsonElement> {
        private final @Nullable Object obj;
        private final Method m;

        private final TypeToken<?> deserializationType;

        @SneakyThrows
        private MethodSetter(Object obj, Method m) {
            this.obj = obj;
            this.m = m;

            this.m.setAccessible(true);

            this.deserializationType = _Reflection.inputType(m);
        }

        @SneakyThrows
        @Override
        public void accept(JsonElement newValue) {
            Object deserialized = Rson.DEFAULT.fromJson(newValue, this.deserializationType);
            this.m.invoke(this.obj, deserialized);
        }
    }

}
