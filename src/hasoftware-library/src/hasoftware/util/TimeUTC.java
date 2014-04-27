package hasoftware.api.classes;

/**
 * Represents the number of milliseconds since midnight on the 1st of March 1900
 */
public class TimeUTC {

    /**
     * Java time is since midnight on the 1st of January 1970 so we need some
     * adjustment to compensate when converting between the two.
     */
    private static final long Adjustment = 0x2012226F000L;

    private long _timeUTC;

    public TimeUTC() {
        this(Adjustment + System.currentTimeMillis());
    }

    public TimeUTC(long timeUTC) {
        _timeUTC = timeUTC;
    }

    public long getTimeUTC() {
        return _timeUTC;
    }

    public void setTimeUTC(long timeUTC) {
        _timeUTC = timeUTC;
    }

}
