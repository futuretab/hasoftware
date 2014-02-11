package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.Location;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class LocationResponse extends Message {

    private int _action;

    private final List<Location> _locations = new LinkedList<>();

    public LocationResponse(int transactionNumber, int action) {
        super(FunctionCode.Location, transactionNumber, CDEFSystemFlags.Response);
        _action = action;
    }

    public LocationResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countLocations = cdefMessage.getU8();
        for (int index = 0; index < countLocations; index++) {
            _locations.add(new Location(cdefMessage));
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_locations.size());
        for (Location location : _locations) {
            location.encode(cdefMessage);
        }
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public List<Location> getLocations() {
        return _locations;
    }
}
