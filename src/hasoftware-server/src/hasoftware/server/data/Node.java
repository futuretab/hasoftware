package hasoftware.server.data;

import hasoftware.server.data.auto._Node;

public class Node extends _Node {

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }
}
