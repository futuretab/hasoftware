package hasoftware.kirk;

import hasoftware.api.DeviceType;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.messages.NotifyResponse;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.api.messages.OutputMessageResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.comms.IPort;
import hasoftware.comms.IPortReader;
import hasoftware.comms.PortFactory;
import hasoftware.comms.Serial;
import hasoftware.configuration.Configuration;
import hasoftware.kirk.messages.Ack;
import hasoftware.kirk.messages.CCFPAck;
import hasoftware.kirk.messages.CCFPAck2;
import hasoftware.kirk.messages.CCFPEof;
import hasoftware.kirk.messages.CCFPMessage;
import hasoftware.kirk.messages.CCFPNak;
import hasoftware.kirk.messages.ChargerIndication;
import hasoftware.kirk.messages.Enquire;
import hasoftware.kirk.messages.Eof;
import hasoftware.kirk.messages.EventType;
import hasoftware.kirk.messages.MSFRelInd;
import hasoftware.kirk.messages.MSFSetupRequest;
import hasoftware.kirk.messages.MSFStatusInd;
import hasoftware.kirk.messages.MSFSubEventType;
import hasoftware.kirk.messages.Nag;
import hasoftware.kirk.messages.StatusType;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KirkProtocol extends AbstractController implements IPortReader {

    private final static Logger logger = LoggerFactory.getLogger(KirkProtocol.class);

    private final static int CCFPReceiveTimeout = 350;
    private final static int CCFPMaxOutstanding = 5;
    private final static int CCFPStatsInterval = 10000;
    private final static int MaxRetries = 3;

    enum State {

        NotConnected, Idle, WaitToSend, WaitCCFPMessage, SendCCFPAck, Received, SendMessage, MessageSent, MessageAcked, SendEOF
    };

    private final String _portDescription;
    private LinkedBlockingQueue<Event> _eventQueue;
    private final LinkedBlockingQueue<CCFPMessage> _receivedMessages;
    private IPort _port;
    private State _state;
    private final Ack ack = new Ack();
    private final Enquire enquireToReceive = new Enquire(0x31);
    private final Enquire enquireToSend = new Enquire(0x32);
    private final Eof eof = new Eof();
    private final HashMap<String, LocalNumber> _localNumbers;
    private long _statsTime;
    private long _statsQueued;
    private long _statsOutstanding;
    private long _statsSent;
    private long _statsSuccess;
    private long _statsError;
    private byte _pcSequenceNumber;
    private final boolean _sendingMultipleMessages;

    public KirkProtocol(Configuration configuration) {
        _portDescription = configuration.getString("Port");
        _receivedMessages = new LinkedBlockingQueue<>();
        _localNumbers = new HashMap<>();
        _statsTime = 0;
        _statsQueued = 0;
        _statsOutstanding = 0;
        _statsSent = 0;
        _statsSuccess = 0;
        _statsError = 0;
        _sendingMultipleMessages = false;
    }

    @Override
    public boolean startUp() {
        logger.debug("startUp");
        if (_portDescription == null) {
            logger.error("Port not set");
            return false;
        }
        if (_eventQueue == null) {
            logger.error("Error EventQueue not set");
            return false;
        }
        _state = State.NotConnected;
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
        _port.close();
        _port = null;
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
                stateMachine(event);
                break;

            case ReceiveMessage:
                Message message = event.getMessage();
                if (!message.isError() && message.isResponse()) {
                    if (message.getFunctionCode() == FunctionCode.OutputMessage) {
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
                    if (outputMessage.getDeviceTypeCode().equals(DeviceType.KIRKDECT.getCode())) {
                        logger.debug("ID:{} DEVICETYPE:{} DATA:{}", outputMessage.getId(), outputMessage.getDeviceTypeCode(), outputMessage.getData());
                        addMessage(outputMessage);
                    }
                }
            }
        }
    }

    private LocalNumber getLocalNumber(String localNo, boolean create) {
        if (_localNumbers.containsKey(localNo)) {
            return _localNumbers.get(localNo);
        }
        if (create) {
            LocalNumber localNumber = new LocalNumber(localNo);
            _localNumbers.put(localNo, localNumber);
            return localNumber;
        }
        return null;
    }

    private void addMessage(OutputMessage outputMessage) {
        // <priority>|<localNo>|<message>|<callbackNo>
        String data = outputMessage.getData();
        String[] parts = data.split("\\|");
        int id = outputMessage.getId();
        String priority = parts[0];
        String localNo = parts[1];
        String message = parts[2];
        String callbackNo = "";
        if (parts.length == 4) {
            callbackNo = parts[3];
        }
        getLocalNumber(localNo, true).getMessages().add(new OutputMessageInfo(id, priority, localNo, message, callbackNo));
        _statsQueued++;
    }

    private void stateMachine(Event event) {
        byte ccfpSequenceNumber = 0;
        CCFPMessage message;
        boolean keepGoing = true;
        while (keepGoing) {
            switch (_state) {
                case NotConnected:
                    // TODO Delay retries to open the port
                    try {
                        _port = PortFactory.getPort(_portDescription);
                        _port.setPortReader((IPortReader) this);
                        _state = State.Idle;
                    } catch (IllegalArgumentException ex) {
                        logger.error("Failed to open port {} - {}", _portDescription, ex.getMessage());
                        keepGoing = false;
                    }
                    break;

                case Idle:
                    _pcSequenceNumber = 0;
                    if (readyToSend() != null) {
                        if (enquireToSend.send(_port)) {
                            _state = State.WaitToSend;
                        } else {
                            keepGoing = false;
                        }
                    } else {
                        if (enquireToReceive.send(_port)) {
                            _state = State.WaitCCFPMessage;
                        } else {
                            keepGoing = false;
                        }
                    }
                    break;

                case WaitToSend:
                    message = getReceivedMessage();
                    if (message == null) {
                        keepGoing = false;
                    } else {
                        if (message instanceof CCFPAck) {
                            _state = State.SendMessage;
                        } else if (message instanceof CCFPNak) {
                            CCFPNak nak = (CCFPNak) message;
                            if (nak.getNag() == Nag.NotReady.getErrorCode()) {
                                // CCFP wants to sent us something perhaps, so try polling
                                if (enquireToReceive.send(_port)) {
                                    _state = State.WaitCCFPMessage;
                                } else {
                                    _state = State.Idle;
                                    keepGoing = false;
                                }
                            } else {
                                logger.error("Unhandled NAK.NAG [{}]" + nak.getNag());
                                _state = State.Idle;
                                keepGoing = false;
                            }
                        } else {
                            logger.error("WaitToSend: Unhandled message [{}]" + message);
                            _state = State.Idle;
                            keepGoing = false;
                        }
                    }
                    break;

                case SendMessage:
                    OutputMessageInfo om = readyToSend();
                    // We assume Format II messaging to the CCFP
                    MSFSetupRequest msr = new MSFSetupRequest();
                    msr.setSetupSpec1(2 & 0x07);            // TODO Proper priorities
                    msr.setLocalNo(om.getLocalNo());
                    msr.setMessage(om.getMessage());
                    msr.setCallbackNo(om.getCallbackNo());
                    msr.setSetupSpec2(0x0D);
                    msr.setSetupSpec3(0x0C);
                    msr.setAlertPattern(0x00);              // TODO Proper priorities
                    msr.setAlertTone(0x02);                 // TODO Proper priorities
                    msr.setAlertTimeout(0x00);              // TODO Proper priorities
                    msr.setDisplayTimeout(0x00);
                    msr.setDateTime("");
                    if (msr.send(_port, _pcSequenceNumber)) {
                        _state = State.MessageSent;
                    } else {
                        keepGoing = false;
                    }
                    break;

                case MessageSent:
                    message = getReceivedMessage();
                    if (message == null) {
                        keepGoing = false;
                    } else {
                        if (message instanceof CCFPAck2) {
                            CCFPAck2 ack2 = (CCFPAck2) message;
                            logger.debug("RX: SN:{} CCFPACK2", ack2.getSequenceNumber());
                            if (ack2.getSequenceNumber() == _pcSequenceNumber) {
                                readyToSend().setSent(true);
                                if (_sendingMultipleMessages) {
                                    _pcSequenceNumber = (byte) (_pcSequenceNumber ^ 0x80);
                                    _state = State.MessageAcked;
                                } else {
                                    _state = State.SendEOF;
                                }
                                _statsSent++;
                                _statsOutstanding++;
                            } else {
                                logger.error("CCFPACK2: Incorrect sequence number");
                                keepGoing = false;
                            }
                        } else if (message instanceof CCFPNak) {
                            CCFPNak nak = (CCFPNak) message;
                            logger.error("NAK [{}]", Nag.find(nak.getNag()).getDescription());
                            keepGoing = false;
                        } else {
                            logger.error("MessageSent: Unhandled message [{}]", message);
                            keepGoing = false;
                        }
                    }
                    break;

                case MessageAcked:
                    if (readyToSend() != null) {
                        _state = State.SendMessage;
                    } else {
                        _state = State.SendEOF;
                    }
                    break;

                case SendEOF:
                    if (eof.send(_port)) {
                        _state = State.Idle;
                    } else {
                        keepGoing = false;
                    }
                    break;

                case WaitCCFPMessage:
                    message = getReceivedMessage();
                    if (message == null) {
                        keepGoing = false;
                    } else {
                        if (message instanceof CCFPEof) {
                            logger.debug("RX: EOF");
                            _state = State.Idle;
                        } else if (message instanceof CCFPNak) {
                            CCFPNak n = (CCFPNak) message;
                            if (n.getNag() != Nag.Idle.getErrorCode()) {
                                logger.warn("RX: Nak {}", n.getNag());
                            }
                            _state = State.Idle;
                            keepGoing = false;
                        } else if (message instanceof MSFStatusInd) {
                            MSFStatusInd si = (MSFStatusInd) message;
                            logger.debug("RX: SN:{} MSFStatusInd LocalNo:{} StatusType:{} StatusBytes:{}", si.getSequenceNumber(), si.getLocalNo(), si.getStatusType(), si.getStatusBytes());
                            if (si.getStatusType() == StatusType.Alarm) {
                                // TODO handle 4040 top alarm button
                            } else if (si.getStatusType() == StatusType.ChargerInd) {
                                if (si.getStatusBytes()[0] == ChargerIndication.InCharger) {
                                    logger.debug("MSFStatusInd LocalNo:{} IN CHARGER", si.getLocalNo());
                                } else if (si.getStatusBytes()[0] == ChargerIndication.OutCharger) {
                                    logger.debug("MSFStatusInd LocalNo:{} OUT CHARGER", si.getLocalNo());
                                }
                            }
                            ccfpSequenceNumber = si.getSequenceNumber();
                            _state = State.SendCCFPAck;
                        } else if (message instanceof MSFRelInd) {
                            MSFRelInd ri = (MSFRelInd) message;
                            logger.debug("RX: SN:{} MSFRelInd LocalNo:{} Reason:{} ", ri.getSequenceNumber(), ri.getLocalNo(), ri.getReason());
                            LocalNumber localNumber = this.getLocalNumber(ri.getLocalNo(), false);
                            if (localNumber != null) {
                                OutputMessageInfo omi = localNumber.getMessages().getFirst();
                                long now = System.currentTimeMillis();
                                float timeTaken = (float) ((now - omi.getSentAt()) / 1000.0);
                                // TODO We are assuming this message has been sent!
                                _statsOutstanding--;
                                if (ri.getReason() == 0x00) {
                                    logger.info("SUCCESS LocalNo:{} Message:{} Time:{}", omi.getLocalNo(), omi.getMessage(), timeTaken);
                                    localNumber.getMessages().remove(omi);
                                    _statsSuccess++;
                                    _statsQueued--;
                                    deleteSingleOutputMessage(omi.getId());
                                } else {
                                    _statsError++;
                                    if (!omi.canRetry()) {
                                        logger.error("FAILURE LocalNo:{} Message:{} Time:{} Reason:{} Deleting:{}", omi.getLocalNo(), omi.getMessage(), timeTaken, ri.getReason(), localNumber.getMessages().size());
                                        _statsQueued -= localNumber.getMessages().size();
                                        deleteAllOutputMessages(localNumber);
                                    } else {
                                        logger.error("RETRY LocalNo:{} Message:{} Time:{} Reason:{}", omi.getLocalNo(), omi.getMessage(), timeTaken, ri.getReason());
                                        omi.setSent(false);
                                    }
                                }
                                // If this local number has no more messages then remove it
                                if (localNumber.getMessages().isEmpty()) {
                                    _localNumbers.remove(localNumber.getLocalNo());
                                }
                            } else {
                                logger.error("WARNING MSFRelInd for unknown LocalNo:{} Reason:{} ", ri.getLocalNo(), ri.getReason());
                            }
                            ccfpSequenceNumber = ri.getSequenceNumber();
                            _state = State.SendCCFPAck;
                        } else {
                            logger.debug("RX: Received unknown message");
                        }
                    }
                    break;

                case SendCCFPAck:
                    ack.setId(ccfpSequenceNumber);
                    if (ack.send(_port)) {
                        _state = State.WaitCCFPMessage;
                    } else {
                        keepGoing = false;
                    }
                    break;
            }
        }
        if (event.getTime()
                > _statsTime) {
            _statsTime = event.getTime() + CCFPStatsInterval;
            logger.debug("STATS State:{} Sent:{} Success:{} Error:{} Queued:{} Outstanding:{}", _state, _statsSent, _statsSuccess, _statsError, _statsQueued, _statsOutstanding);
        }
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

    /**
     * Sends a request to delete all queued OutputMessages for this local number
     *
     * @param localNumber
     */
    private void deleteAllOutputMessages(LocalNumber localNumber) {
        OutputMessageRequest outputMessageRequest = new OutputMessageRequest();
        outputMessageRequest.setAction(CDEFAction.Delete);
        for (OutputMessageInfo omi : localNumber.getMessages()) {
            outputMessageRequest.getIds().add(omi.getId());
        }
        Event event = new Event(hasoftware.util.EventType.SendMessage);
        event.setMessage(outputMessageRequest);
        _eventQueue.add(event);
    }

    private OutputMessageInfo readyToSend() {
        if (_statsOutstanding < CCFPMaxOutstanding) {
            // TODO Is there an available messsage to send to an idle dect phone?
            for (LocalNumber localNumber : _localNumbers.values()) {
                if (!localNumber.isBusy()) {
                    return localNumber.getMessages().getFirst();
                }
            }
        }
        return null;
    }

    private CCFPMessage getReceivedMessage() {
        CCFPMessage message = null;
        try {
            message = _receivedMessages.poll(CCFPReceiveTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            logger.error("Exception in getReceivedMessage: {}", ex.getMessage());
        }
        return message;
    }

    private final int MaxBufferSize = 1024;
    private final byte[] _buffer = new byte[MaxBufferSize];
    private int _offset = 0;

    @Override
    public void onReceive(final byte[] buffer, final int len) {
        for (int i = 0; i < len; i++, _offset++) {
            // TODO buffer overrun
            _buffer[_offset] = buffer[i];
        }
        // 1 byte ACK
        if (_offset >= 1 && _buffer[0] == Serial.Ack) {
            _receivedMessages.offer(new CCFPAck());
            System.arraycopy(_buffer, 1, _buffer, 0, _offset - 1);
            _offset -= 1;
            return;
        }
        // 2 byte NAK
        if (_offset >= 2 && _buffer[0] == Serial.Nak) {
            _receivedMessages.offer(new CCFPNak(_buffer[1]));
            System.arraycopy(_buffer, 2, _buffer, 0, _offset - 2);
            _offset -= 2;
            return;
        }
        // 2 byte ACK
        if (_offset >= 2 && (_buffer[0] == (byte) 0x00 || _buffer[0] == (byte) 0x80) && _buffer[1] == Serial.Ack) {
            _receivedMessages.offer(new CCFPAck2(_buffer[0]));
            System.arraycopy(_buffer, 2, _buffer, 0, _offset - 2);
            _offset -= 2;
            return;
        }
        // 3 byte EOF
        if (_offset >= 3 && _buffer[0] == Serial.Dle && _buffer[1] == Serial.Etb) {
            _receivedMessages.offer(new CCFPEof());
            System.arraycopy(_buffer, 3, _buffer, 0, _offset - 3);
            _offset -= 3;
            return;
        }
        // Reply
        if (_offset >= 7 && (_buffer[0] == (byte) 0x00 || _buffer[0] == (byte) 0x80)) {
            int length = _buffer[3];
            int index = 4;
            int count = 4;
            if (length == Serial.Dle) {
                length = _buffer[4] + 8;
                index = 5;
                count = 5;
            } else {
                length += 7;
            }
            while (index <= _offset && count < length) {
                if (count == (length - 3) || _buffer[index] != Serial.Dle) {
                    index++;
                    count++;
                } else {
                    index++;
                    index++;
                    count++;
                }
            }
            if (count == length) {
                if (_buffer[1] == EventType.MSF && _buffer[2] == MSFSubEventType.MSFStatusInd) {
                    _receivedMessages.offer(new MSFStatusInd(_buffer));
                    System.arraycopy(_buffer, count, _buffer, 0, _offset - count);
                    _offset -= count;
                } else if (_buffer[1] == EventType.MSF && _buffer[2] == MSFSubEventType.MSFRelInd) {
                    _receivedMessages.offer(new MSFRelInd(_buffer));
                    System.arraycopy(_buffer, count, _buffer, 0, _offset - count);
                    _offset -= count;
                } else {
                    debugBuffer("RX: UNKNOWN MESSAGE", _buffer, count);
                    System.arraycopy(_buffer, count, _buffer, 0, _offset - count);
                    _offset -= count;
                }
            }
        }
    }

    public static void debugBuffer(String prefix, byte[] buffer, final int length) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(" [ ");
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%1$02X ", buffer[i]));
        }
        sb.append("]");
        logger.error(sb.toString());

    }

    class LocalNumber {

        private final String _localNo;
        private final LinkedList<OutputMessageInfo> _messages;

        public LocalNumber(String localNo) {
            _localNo = localNo;
            _messages = new LinkedList<>();
        }

        public String getLocalNo() {
            return _localNo;
        }

        public LinkedList<OutputMessageInfo> getMessages() {
            return _messages;
        }

        public boolean isBusy() {
            return _messages.getFirst().isSent();
        }
    }

    class OutputMessageInfo {

        private final int _id;
        private final String _priority;
        private final String _localNo;
        private final String _message;
        private final String _callbackNo;
        private boolean _sent;
        private long _sentAt;
        private int _retries;

        public OutputMessageInfo(int id, String priority, String localNo, String message, String callbackNo) {
            _id = id;
            _priority = priority;
            _localNo = localNo;
            _message = message;
            _callbackNo = callbackNo;
            _sent = false;
            _sentAt = 0;
            _retries = 0;
        }

        public int getId() {
            return _id;
        }

        public String getPriority() {
            return _priority;
        }

        public String getLocalNo() {
            return _localNo;
        }

        public String getMessage() {
            return _message;
        }

        public String getCallbackNo() {
            return _callbackNo;
        }

        public void setSent(boolean sent) {
            _sent = sent;
            if (sent) {
                _sentAt = System.currentTimeMillis();
            } else {
                _sentAt = 0;
            }
        }

        public boolean isSent() {
            return _sent;
        }

        public long getSentAt() {
            return _sentAt;
        }

        public boolean canRetry() {
            _retries++;
            return (_retries < KirkProtocol.MaxRetries);
        }
    }
}
