package hasoftware.server.data;

import hasoftware.server.data.auto._OutputDevice;

public class OutputDevice extends _OutputDevice {

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }
}
