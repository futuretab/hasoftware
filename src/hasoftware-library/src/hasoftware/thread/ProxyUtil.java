package hasoftware.thread;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

public class ProxyUtil {
    public static <T> T broadcast(Class<T> clazz, final Iterable<? extends T> receivers) {
        return makeProxy(clazz, new InvocationHandlerImpl() {
            @Override
            protected Object dispatch(Method method, Object[] args) throws Throwable {
                for (T receiver : receivers) {
                    send(receiver, method, args);
                }
                return null;
            }
        });
    }
    
    public static <T> T decouple(final ExecutorService executorService, Class<T> clazz, final T target) {
        return makeProxy(clazz, new DecoupledInvocationHandlerImpl<>(executorService, target));
    }
    
    public static <T> T makeProxy(Class<T> clazz, InvocationHandler handler) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?>[] interfaces = new Class<?>[] { clazz };
        return clazz.cast(Proxy.newProxyInstance(loader, interfaces, handler));
    }
    
    private static final class DecoupledInvocationHandlerImpl<T> extends InvocationHandlerImpl {
        private final ExecutorService _executorService;
        private final T _target;

        public DecoupledInvocationHandlerImpl(ExecutorService executorService, T target) {
            super();
            _executorService = executorService;
            _target = target;
        }

        public boolean isDecoupledTo(ExecutorService executorService) {
            return _executorService == executorService;
        }

        @Override
        protected final Object dispatch(final Method method, final Object[] args) throws Throwable {
            _executorService.execute(new Runnable() {
                @Override
                public void run() {
                    send(_target, method, args);
                }
            });
            return null;
        }
    }
}
