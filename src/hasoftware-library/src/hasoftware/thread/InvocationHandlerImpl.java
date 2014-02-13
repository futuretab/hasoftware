package hasoftware.thread;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class InvocationHandlerImpl implements InvocationHandler {
    private static final boolean _dumpExceptions = Boolean.getBoolean("nasco.dev");
    
    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            if (args == null) {
                return method.invoke(this);
            }
            Object other = args[0];
            return Boolean.valueOf(Proxy.isProxyClass(other.getClass()) && equals(Proxy.getInvocationHandler(other)));
        }
        return dispatch(method, args);
    }
    
    protected abstract Object dispatch(Method method, Object[] args) throws Throwable;
    
    protected static void send(Object object, Method method, Object[] args) {
        try {
            method.invoke(object, args);
        } catch (InvocationTargetException e) {
            if (_dumpExceptions) {
                System.err.println("Proxied method invocation threw exception: " + e.getMessage());
                e.getCause().printStackTrace(System.err);
            }
        } catch (Throwable t) {
            if (_dumpExceptions) {
                System.err.println("Proxied method invocation failed: " + t.getMessage());
                t.printStackTrace(System.err);
            }
        }
    }
}
