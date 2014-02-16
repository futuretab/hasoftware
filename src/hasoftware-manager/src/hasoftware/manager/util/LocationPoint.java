package hasoftware.manager.util;

import hasoftware.api.classes.Location;
import hasoftware.api.classes.Point;

public class LocationPoint {

    private final Location _location;
    private final Point _point;

    public LocationPoint(Location location, Point point) {
        _location = location;
        _point = point;
    }

    public boolean isLocation() {
        return (_location != null);
    }

    public boolean isPoint() {
        return (_point != null);
    }

    public Location getLocation() {
        return _location;
    }

    public Point getPoint() {
        return _point;
    }
}
