package hasoftware.thread;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class Notifier<E> {
    private Set<E> _listeners = new HashSet<>();
    private ExecutorService _executorService;
    private E _proxy;

    public Notifier(ExecutorService executorService, Class<E> clazz) {
        _executorService = executorService;
        _proxy = decoupleSerial(clazz, ProxyUtil.broadcast(clazz, _listeners));
    }

    public E getProxy() {
        return _proxy;
    }

    public void add(final E listener) {
        _executorService.execute(new Runnable() {
            @Override
            public void run() {
                _listeners.add(listener);
            }
        });
    }

    public void remove(final E listener) {
        _executorService.execute(new Runnable() {
            @Override
            public void run() {
                _listeners.remove(listener);
            }
        });
    }
    
    private <T> T decoupleSerial(Class<T> clazz, T listener) {
        return ProxyUtil.decouple(_executorService, clazz, listener);
    }
}