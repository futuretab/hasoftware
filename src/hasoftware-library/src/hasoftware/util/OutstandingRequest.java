package hasoftware.util;

public class OutstandingRequest<T> {

    public final int transactionNumber;
    public final T data;

    public OutstandingRequest(int transactionNumber, T data) {
        this.transactionNumber = transactionNumber;
        this.data = data;
    }
}
