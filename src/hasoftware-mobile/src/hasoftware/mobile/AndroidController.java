package hasoftware.mobile;

import hasoftware.api.DeviceType;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.api.messages.OutputMessageResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
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

public class AndroidController extends AbstractController {

    private final static Logger logger = LoggerFactory.getLogger(AndroidController.class);

    private final static int AndroidStatsInterval = 10000;

    private ExecutorService _executorService;
    private LinkedBlockingQueue<Event> _eventQueue;
    private final Object _stats;
    private long _statsTime;
    private long _statsQueued;
    private long _statsSuccess;
    private long _statsError;

    public AndroidController(Configuration configuration) {
        _stats = new Object();
        _statsTime = 0;
        _statsQueued = 0;
        _statsSuccess = 0;
        _statsError = 0;
    }

    @Override
    public boolean startUp() {
        logger.debug("startUp");
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
                    _statsTime = event.getTime() + AndroidStatsInterval;
                    synchronized (_stats) {
                        logger.debug("ANDROID STATS Success:{} Error:{} Queued:{}", _statsSuccess, _statsError, _statsQueued);
                    }
                }
                break;

            case ReceiveMessage:
                Message message = event.getMessage();
                if (!message.isError() && message.isResponse()) {
                    // We will assume SMS controller is taking care of login/notify/etc
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
                    if (outputMessage.getDeviceTypeCode().equals(DeviceType.ANDROID.getCode())) {
                        logger.debug("ID:{} DEVICETYPE:{} DATA:{}", outputMessage.getId(), outputMessage.getDeviceTypeCode(), outputMessage.getData());
                        addMessage(outputMessage);
                    }
                }
            }
        }
    }

    private void addMessage(OutputMessage outputMessage) {
        // <priority>|<api key>|<message>
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
        _executorService.execute(new Runnable() {
            @Override
            public void run() {
                sendAndoidMessage(phoneMessageInfo);
            }
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

    private void sendAndoidMessage(final PhoneMessageInfo phoneMessageInfo) {
        // TODO Retry
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("https://www.notifymyandroid.com/publicapi/notify");
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        NameValuePair[] data = {
            new NameValuePair("apikey", phoneMessageInfo.getPhoneNumber()),
            new NameValuePair("application", "NASCO Manager"),
            new NameValuePair("event", phoneMessageInfo.getMessage()),
            new NameValuePair("description", "Description goes here")
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
                logger.error("sendAndoidMessage Response Stats:{} {}", post.getStatusCode(), post.getStatusLine());
                synchronized (_stats) {
                    _statsQueued--;
                    _statsError++;
                }
            }
        } catch (IOException ex) {
            logger.error("sendAndoidMessage Exception: {}", ex.getMessage());
            _statsQueued--;
            _statsError++;
        } finally {
            post.releaseConnection();
            deleteSingleOutputMessage(phoneMessageInfo.getId());
        }
    }
}
