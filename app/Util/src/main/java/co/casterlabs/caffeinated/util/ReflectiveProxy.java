package co.casterlabs.caffeinated.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@AllArgsConstructor
public class ReflectiveProxy implements InvocationHandler {
    private Object instance;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return ReflectionLib.invokeMethod(this.instance, method.getName(), args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProxy(@NonNull Class<T> clazz, @NonNull Object instance) {
        return (T) Proxy.newProxyInstance(
            ReflectiveProxy.class.getClassLoader(),
            new Class[] {
                    clazz
            },
            new ReflectiveProxy(instance)
        );
    }

}
