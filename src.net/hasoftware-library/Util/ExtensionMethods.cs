﻿using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public static class ExtensionMethods
    {
        private static DateTime Jan1st1970 = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        public static long CurrentTimeMillis(this DateTime d)
        {
            return (long)((DateTime.UtcNow - Jan1st1970).TotalMilliseconds);
        }
    }
}
