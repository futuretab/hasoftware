package hasoftware.mobile;

import hasoftware.api.DeviceType;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.api.messages.NotifyResponse;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.api.messages.OutputMessageResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSController extends AbstractController {

    private final static Logger logger = LoggerFactory.getLogger(SMSController.class);

    private final static int SMSStatsInterval = 10000;
    private final static int MaxRetries = 3;

    private final String _smsAddress;
    private final String _smsPassword;
    private final String _serverUsername;
    private final String _serverPassword;
    private ExecutorService _executorService;
    private LinkedBlockingQueue<Event> _eventQueue;
    private final Object _stats;
    private long _statsTime;
    private long _statsQueued;
    private long _statsSuccess;
    private long _statsError;

    public SMSController(Configuration configuration) {
        _smsAddress = configuration.getString("SMSAddress");
        _smsPassword = configuration.getString("SMSPassword");
        _serverUsername = configuration.getString("Username");
        _serverPassword = configuration.getString("Password");
        _stats = new Object();
        _statsTime = 0;
        _statsQueued = 0;
        _statsSuccess = 0;
        _statsError = 0;
    }

    @Override
    public boolean startUp() {
        logger.debug("startUp");
        if (StringUtil.isNullOrEmpty(_smsAddress)) {
            logger.error("SMSAddress not set");
            return false;
        }
        if (StringUtil.isNullOrEmpty(_smsPassword)) {
            logger.error("SMSPassword not set");
            return false;
        }
        if (StringUtil.isNullOrEmpty(_serverUsername)) {
            logger.error("Username not set");
            return false;
        }
        if (StringUtil.isNullOrEmpty(_serverPassword)) {
            logger.error("Password not set");
            return false;
        }
        if (_eventQueue == null) {
            logger.error("Error EventQueue not set");
            return false;
        }
        _executorService = Executors.newSingleThreadExecutor();
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
                        logger.debug("SMS STATS Success:{} Error:{} Queued:{}", _statsSuccess, _statsError, _statsQueued);
                    }
                }
                break;

            case Connect: {
                // Send LoginRequest
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(_serverUsername);
                loginRequest.setPassword(_serverPassword);
                Event e = new Event(EventType.SendMessage);
                e.setMessage(loginRequest);
                _eventQueue.add(e);
            }
            break;

            case ReceiveMessage:
                Message message = event.getMessage();
                if (!message.isError() && message.isResponse()) {
                    if (message.getFunctionCode() == FunctionCode.Login) {
                        // Send NotifyRequest for OutputMessage
                        NotifyRequest notifyRequest = new NotifyRequest();
                        notifyRequest.getFunctionCodes().add(FunctionCode.OutputMessage);
                        Event e = new Event(EventType.SendMessage);
                        e.setMessage(notifyRequest);
                        _eventQueue.add(e);
                        // Send OutputMessage(list)
                        OutputMessageRequest outputMessageRequest = new OutputMessageRequest();
                        outputMessageRequest.setAction(CDEFAction.List);
                        e = new Event(EventType.SendMessage);
                        e.setMessage(outputMessageRequest);
                        _eventQueue.add(e);
                    } else if (message.getFunctionCode() == FunctionCode.OutputMessage) {
                        handleOutputMessageResponse((OutputMessageResponse) message);
                    } else if (message.getFunctionCode() == FunctionCode.Notify) {
                        handleNotifyResponse((NotifyResponse) message);
                    }
                }
                break;
        }
        return true;
    }

    private void handleNotifyResponse(NotifyResponse notifyResponse) {
        if (notifyResponse.getAction() == CDEFAction.Create) {
            logger.debug("handleNotifyResponse[Create] - {}", notifyResponse.getIds());
            // When we get a notify for created output messages go get them
            OutputMessageRequest outputMessageRequest = new OutputMessageRequest();
            outputMessageRequest.setAction(CDEFAction.List);
            outputMessageRequest.getIds().addAll(notifyResponse.getIds());
            Event event = new Event(hasoftware.util.EventType.SendMessage);
            event.setMessage(outputMessageRequest);
            _eventQueue.add(event);
        }
    }

    private void handleOutputMessageResponse(OutputMessageResponse outputMessageResponse) {
        logger.debug("handleOutputMessageResponse");
        if (outputMessageResponse.getAction() == CDEFAction.List) {
            if (!outputMessageResponse.getOutputMessages().isEmpty()) {
                for (OutputMessage outputMessage : outputMessageResponse.getOutputMessages()) {
                    if (outputMessage.getDeviceTypeCode().equals(DeviceType.SMS.getCode())) {
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
        synchronized (_stats) {
            _statsQueued++;
        }
        _executorService.execute(() -> {
            sendSMSMessage(phoneMessageInfo);
        });
    }

    /**
     * Sends a request to delete a single OutputMessage
     *
     * @param id The Id of the OutputMessage to delete
     */
    private void deleteSingleOutputMessage(int id) {
        OutputMessageRequest outputMessageRequest = new OutputMessageRequest();
        outputMessageRequest.setAction(CDEFAction.Delete);
        outputMessageRequest.getIds().add(id);
        Event event = new Event(hasoftware.util.EventType.SendMessage);
        event.setMessage(outputMessageRequest);
        _eventQueue.add(event);
    }

    private void sendSMSMessage(final PhoneMessageInfo phoneMessage) {
        // TODO Retry
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(_smsAddress);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        NameValuePair[] data = {
            new NameValuePair("password", _smsPassword),
            new NameValuePair("phone_number", phoneMessage.getPhoneNumber()),
            new NameValuePair("sms", phoneMessage.getMessage())
        };
        post.setRequestBody(data);
        try {
            client.executeMethod(post);
            if (post.getStatusCode() == HttpStatus.SC_OK) {
                InputStream in = post.getResponseBodyAsStream();
                byte b[] = new byte[1024];
                StringBuilder html = new StringBuilder("");
                while (in.read(b) != -1) {
                    html.append((new String(b)).toString());
                    b = new byte[1024];
                }
                logger.debug("Resonse: {}", html.toString());
                synchronized (_stats) {
                    _statsQueued--;
                    _statsSuccess++;
                }
            } else {
                logger.error("sendSMSMessage Response Stats:{} {}", post.getStatusCode(), post.getStatusLine());
                synchronized (_stats) {
                    _statsQueued--;
                    _statsError++;
                }
            }
        } catch (IOException ex) {
            logger.error("sendSMSMessage Exception: {}", ex.getMessage());
            _statsQueued--;
            _statsError++;
        } finally {
            post.releaseConnection();
            deleteSingleOutputMessage(phoneMessage.getId());
        }
    }
}
