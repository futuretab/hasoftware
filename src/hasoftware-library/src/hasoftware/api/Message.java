package hasoftware.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hasoftware.api.messages.ErrorResponse;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Message {

    private static AtomicInteger TransactionNumber = new AtomicInteger(1);

    private int _functionCode;
    private int _transactionNumber;
    private int _systemFlags;

    /**
     * Usually used for requests
     *
     * @param functionCode
     * @param systemFlags
     */
    protected Message(int functionCode, int systemFlags) {
        _functionCode = functionCode;
        _transactionNumber = TransactionNumber.getAndIncrement();
        _systemFlags = systemFlags;
    }

    /**
     * Usually used for responses to requests
     *
     * @param functionCode
     * @param transactionNumber
     * @param systemFlags
     */
    protected Message(int functionCode, int transactionNumber, int systemFlags) {
        _functionCode = functionCode;
        _transactionNumber = transactionNumber;
        _systemFlags = systemFlags;
    }

    protected Message(CDEFMessage cdefMessage) {
        _functionCode = cdefMessage.getInt(0);
        _transactionNumber = cdefMessage.getInt();
        _systemFlags = cdefMessage.getInt();
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.clear();
        cdefMessage.putInt(_functionCode);
        cdefMessage.putInt(_transactionNumber);
        cdefMessage.putInt(_systemFlags);
    }

    public int getFunctionCode() {
        return _functionCode;
    }

    public void setFunctionCode(int functionCode) {
        _functionCode = functionCode;
    }

    public int getTransactionNumber() {
        return _transactionNumber;
    }

    public void setTransactionNumber(int transactionNumber) {
        _transactionNumber = transactionNumber;
    }

    public int getSystemFlags() {
        return _systemFlags;
    }

    public void setSystemFlags(int systemFlags) {
        _systemFlags = systemFlags;
    }

    @JsonIgnore
    public boolean isRequest() {
        return ((_systemFlags & CDEFSystemFlags.Response) == 0);
    }

    @JsonIgnore
    public boolean isResponse() {
        return ((_systemFlags & CDEFSystemFlags.Response) == CDEFSystemFlags.Response);
    }

    @JsonIgnore
    public boolean isError() {
        return ((_systemFlags & CDEFSystemFlags.Error) == CDEFSystemFlags.Error);
    }

    public ErrorResponse createErrorResponse() {
        ErrorResponse errorResponse = new ErrorResponse(_transactionNumber);
        errorResponse.setFunctionCode(_functionCode);
        return errorResponse;
    }
}
