package hasoftware.server.data;

import hasoftware.server.data.auto._InputEvent;

public class InputEvent extends _InputEvent {

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }
}
