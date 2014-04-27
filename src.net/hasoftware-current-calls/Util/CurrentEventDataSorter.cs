using System.Collections.Generic;

namespace hasoftware_current_calls.Util
{
    public class CurrentEventDataSorter : IComparer<CurrentEventData>
    {
        public int Compare(CurrentEventData x, CurrentEventData y)
        {
            if (x.CurrentEvent.Id == y.CurrentEvent.Id) return 0;
            if (x.CurrentEvent.Point.Priority < y.CurrentEvent.Point.Priority) return -1;
            if (x.CurrentEvent.Point.Priority > y.CurrentEvent.Point.Priority) return 1;
            if (x.CurrentEvent.CreatedOn < y.CurrentEvent.CreatedOn) return -1;
            if (x.CurrentEvent.CreatedOn > y.CurrentEvent.CreatedOn) return 1;
            return 0;
        }
    }
}
