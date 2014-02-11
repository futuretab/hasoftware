package hasoftware.api;

public class PermissionException extends Exception {

    public PermissionException(String permission) {
        super("Unassigned permission [" + permission + "]");
    }
}
