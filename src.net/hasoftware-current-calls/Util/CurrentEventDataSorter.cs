using hasoftware.Classes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware_current_calls.Util
{
    public class CurrentEventDataSorter : IComparer<CurrentEventData>
    {
        public int Compare(CurrentEventData x, CurrentEventData y)
        {
            if (x.CurrentEvent.Id == y.CurrentEvent.Id) return 0;
            if (x.CurrentEvent.Point.Priority < y.CurrentEvent.Point.Priority) return -1;
            if (x.CurrentEvent.Point.Priority > y.CurrentEvent.Point.Priority) return 1;
            if (x.CurrentEvent.CreatedOn.Time < y.CurrentEvent.CreatedOn.Time) return -1;
            if (x.CurrentEvent.CreatedOn.Time > y.CurrentEvent.CreatedOn.Time) return 1;
            return 0;
        }
    }
}
