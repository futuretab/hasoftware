package hasoftware.server.data;

import hasoftware.server.data.auto._ActiveEvent;

public class ActiveEvent extends _ActiveEvent {

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }
}
