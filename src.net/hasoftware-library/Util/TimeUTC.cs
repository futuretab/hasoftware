using System;

namespace hasoftware.Util
{
    public class TimeUtc
    {
        public static readonly long Null = long.MinValue;

        private static readonly long Adjustment = 0x2012226F000L;

        public long Time { get; set; }

        public TimeUtc()
        {
            Time = Adjustment + DateTime.Now.CurrentTimeMillis();
        }

        public TimeUtc(long timeUtc)
        {
            Time = timeUtc;
        }
    }
}
