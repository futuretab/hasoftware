package hasoftware.server;

import hasoftware.api.messages.NotifyResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifications {

    private final static Logger logger = LoggerFactory.getLogger(Notifications.class);

    private final static HashMap<Integer, ArrayList<ServerHandler>> _codeMap;

    static {
        _codeMap = new HashMap<>();
    }

    /**
     * Add notification codes for a given handler
     *
     * @param handler
     * @param functionCodes
     */
    public static void add(ServerHandler handler, List<Integer> functionCodes) {
        synchronized (_codeMap) {
            for (Integer functionCode : functionCodes) {
                ArrayList<ServerHandler> handlers;
                if (_codeMap.containsKey(functionCode)) {
                    handlers = _codeMap.get(functionCode);
                } else {
                    handlers = new ArrayList<>();
                    _codeMap.put(functionCode, handlers);
                }
                if (!handlers.contains(handler)) {
                    logger.debug("Add [FC:{} => H:{}]", functionCode, handler.getHandlerId());
                    handlers.add(handler);
                }
            }
        }
    }

    /**
     * Remove all notification codes for a given handler
     *
     * @param handler
     */
    public static void remove(ServerHandler handler) {
        synchronized (_codeMap) {
            for (Integer code : _codeMap.keySet()) {
                ArrayList<ServerHandler> handlers = _codeMap.get(code);
                if (handlers.contains(handler)) {
                    logger.debug("Remove [FC:{} => H:{}]", code, handler.getHandlerId());
                    handlers.remove(handler);
                }
            }
        }
    }

    /**
     * Send a notification to all interested handlers
     *
     * @param functionCode
     * @param action
     * @param ids
     */
    public static void notify(int functionCode, int action, List<Integer> ids) {
        NotifyResponse message = new NotifyResponse();
        message.setNotifyFunctionCode(functionCode);
        message.setAction(action);
        message.getIds().addAll(ids);
        synchronized (_codeMap) {
            if (_codeMap.containsKey(functionCode)) {
                ArrayList<ServerHandler> handlers = _codeMap.get(functionCode);
                for (ServerHandler handler : handlers) {
                    logger.debug("Notify [FC:{} => H:{}]", functionCode, handler.getHandlerId());
                    handler.send(message);
                }
            }
        }
    }
}
