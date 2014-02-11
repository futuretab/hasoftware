package hasoftware.server.data;

import hasoftware.server.data.auto._OutputEvent;

public class OutputEvent extends _OutputEvent {

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }
}
