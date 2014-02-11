package hasoftware.cdef;

public class CDEFAction {

    public static final int None = 0;
    public static final int Create = 1;
    public static final int Delete = 2;
    public static final int List = 3;
    public static final int Update = 4;

    private static String[] Actions = new String[]{
        "None",
        "Create",
        "Delete",
        "List",
        "Update"
    };

    public static String getActionStr(int action) {
        return Actions[action];
    }
}
