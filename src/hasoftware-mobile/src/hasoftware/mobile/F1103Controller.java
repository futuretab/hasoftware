package hasoftware.mobile;

import hasoftware.api.DeviceType;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.messages.OutputMessageResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import hasoftware.util.StringUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class F1103Controller extends AbstractController {

    private final static Logger logger = LoggerFactory.getLogger(F1103Controller.class);

    private final static int SMSStatsInterval = 10000;
    private final static int MaxRetries = 3;

    private final String _smsPort;
    private LinkedBlockingQueue<Event> _eventQueue;
    private ExecutorService _executorService;
    private final Object _stats;
    private long _statsQueued;
    private long _statsSuccess;
    private long _statsError;
    private long _statsTime;
    private boolean _enabled;

    public F1103Controller(Configuration configuration) {
        _enabled = configuration.getBoolean("F1103.Enabled", false);
        _smsPort = configuration.getString("Port");
        _stats = new Object();
        _statsTime = 0;
        _statsQueued = 0;
        _statsSuccess = 0;
        _statsError = 0;
    }

    @Override
    public boolean startUp() {
        logger.debug("startUp");
        if (_enabled) {
            if (StringUtil.isNullOrEmpty(_smsPort)) {
                logger.error("Port not set");
                return false;
            }
            if (_eventQueue == null) {
                logger.error("Error EventQueue not set");
                return false;
            }
            _executorService = Executors.newSingleThreadExecutor();
        }
        return true;
    }

    @Override
    public boolean readyToShutDown() {
        logger.debug("readyToShutDown");
        return true;
    }

    @Override
    public boolean shutDown() {
        logger.debug("shutDown");
        return true;
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        logger.debug("setEventQueue");
        _eventQueue = eventQueue;
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        switch (event.getType()) {
            case TimeCheck:
                if (event.getTime() > _statsTime) {
                    _statsTime = event.getTime() + SMSStatsInterval;
                    synchronized (_stats) {
                        logger.debug("F1103 STATS Success:{} Error:{} Queued:{}", _statsSuccess, _statsError, _statsQueued);
                    }
                }
                break;

            case ReceiveMessage:
                Message message = event.getMessage();
                if (!message.isError() && message.isResponse()) {
                    if (message.getFunctionCode() == FunctionCode.OutputMessage) {
                        handleOutputMessageResponse((OutputMessageResponse) message);
                    }
                }
                break;
        }
        return true;
    }

    private void handleOutputMessageResponse(OutputMessageResponse outputMessageResponse) {
        logger.debug("handleOutputMessageResponse");
        if (outputMessageResponse.getAction() == CDEFAction.List) {
            if (!outputMessageResponse.getOutputMessages().isEmpty()) {
                for (OutputMessage outputMessage : outputMessageResponse.getOutputMessages()) {
                    if (outputMessage.getDeviceTypeCode().equals(DeviceType.SMS2.getCode())) {
                        logger.debug("ID:{} DEVICETYPE:{} DATA:{}", outputMessage.getId(), outputMessage.getDeviceTypeCode(), outputMessage.getData());
                        addMessage(outputMessage);
                    }
                }
            }
        }
    }

    private void addMessage(OutputMessage outputMessage) {
        // <priority>|<phone number>|<message>
        String data = outputMessage.getData();
        String[] parts = data.split("\\|");
        int id = outputMessage.getId();
        String priority = parts[0];
        String phoneNumber = parts[1];
        String message = parts[2];
        final PhoneMessageInfo phoneMessageInfo = new PhoneMessageInfo(id, phoneNumber, message);

        // TODO
    }
}
