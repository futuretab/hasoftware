using hasoftware.Classes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware_current_calls.Util
{
    public class CurrentEventData
    {
        public bool NewFlag { get; set; }
        public CurrentEvent CurrentEvent { get; set; }
        public PriorityData Priority { get; set; }
        public bool AcknowledgedFlag { get; set; }
    }
}
