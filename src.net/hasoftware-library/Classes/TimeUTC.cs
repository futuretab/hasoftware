using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using hasoftware.Util;

namespace hasoftware.Classes
{
    public class TimeUtc
    {
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
