package io.github.genie.sql.builder.reflect;

import io.github.genie.sql.builder.meta.Attribute;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(fluent = true)
public final class InstanceInvocationHandler implements InvocationHandler {
    private static final Method EQUALS = getEqualsMethod();
    private final Property[] properties;
    private final Class<?> resultType;
    private final Map<Method, Object> data;

    @SneakyThrows
    @NotNull
    private static Method getEqualsMethod() {
        return Object.class.getMethod("equals", Object.class);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (data.containsKey(method)) {
            return data.get(method);
        }
        if (EQUALS.equals(method)) {
            return equals(proxy, args[0]);
        }
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        if (method.isDefault()) {
            return ReflectUtil.invokeDefaultMethod(proxy, method, args);
        }
        throw new AbstractMethodError(method.toString());
    }

    @NotNull
    private Object equals(Object proxy, Object other) {
        if (proxy == other) {
            return true;
        }
        if (other == null || !Proxy.isProxyClass(other.getClass())) {
            return false;
        }
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(other);
        if (invocationHandler.getClass() != InstanceInvocationHandler.class) {
            return false;
        }
        InstanceInvocationHandler handler = (InstanceInvocationHandler) invocationHandler;
        return resultType.equals(handler.resultType) && data.equals(handler.data);
    }


    @Override
    public int hashCode() {
        int result = data.hashCode();
        result = 31 * result + resultType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Proxy(" + resultType + ")";
    }
}
