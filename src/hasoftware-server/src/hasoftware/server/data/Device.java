package hasoftware.server.data;

import hasoftware.server.data.auto._Device;

public class Device extends _Device {

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }
}
