package co.casterlabs.koi.api.listener;

import java.lang.reflect.Method;

import lombok.NonNull;
import lombok.SneakyThrows;

public class KoiEventUtil {

    @SneakyThrows
    public static void reflectInvoke(@NonNull Object listener, @NonNull Object event) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(KoiEventHandler.class) &&
                (method.getParameterCount() == 1) &&
                method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                method.setAccessible(true);
                method.invoke(listener, event);
            }
        }
    }

}
