package hasoftware.api;

public class NotLoggedInException extends Exception {

    public NotLoggedInException() {
        super("Not logged in");
    }
}
