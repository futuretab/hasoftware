package hasoftware.server;

import hasoftware.api.messages.NotifyResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifications {

    private final static Logger logger = LoggerFactory.getLogger(Notifications.class);

    private final static HashMap<Integer, ArrayList<INotificationTarget>> _codeMap;

    static {
        _codeMap = new HashMap<>();
    }

    /**
     * Add notification codes for a given handler
     *
     * @param notificationTarget
     * @param functionCodes
     */
    public static void add(INotificationTarget notificationTarget, List<Integer> functionCodes) {
        synchronized (_codeMap) {
            for (Integer functionCode : functionCodes) {
                ArrayList<INotificationTarget> targets;
                if (_codeMap.containsKey(functionCode)) {
                    targets = _codeMap.get(functionCode);
                } else {
                    targets = new ArrayList<>();
                    _codeMap.put(functionCode, targets);
                }
                if (!targets.contains(notificationTarget)) {
                    logger.debug("Add [FC:{} => H:{}]", functionCode, notificationTarget.getId());
                    targets.add(notificationTarget);
                }
            }
        }
    }

    /**
     * Remove all notification codes for a given handler
     *
     * @param notificationTarget
     */
    public static void remove(INotificationTarget notificationTarget) {
        synchronized (_codeMap) {
            for (Integer code : _codeMap.keySet()) {
                ArrayList<INotificationTarget> targets = _codeMap.get(code);
                if (targets.contains(notificationTarget)) {
                    logger.debug("Remove [FC:{} => H:{}]", code, notificationTarget.getId());
                    targets.remove(notificationTarget);
                }
            }
        }
    }

    /**
     * Send a notification to all interested targets
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
                ArrayList<INotificationTarget> targets = _codeMap.get(functionCode);
                for (INotificationTarget notificationTarget : targets) {
                    logger.debug("Notify [FC:{} => H:{}]", functionCode, notificationTarget.getId());
                    notificationTarget.send(message);
                }
            }
        }
    }
}
