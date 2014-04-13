using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware_current_calls.Util
{
    public class PriorityData
    {
        public int Level { get; set; }
        public string Code { get; set; }
        public string Description { get; set; }
        public Color ForeColor { get; set; }
        public Color BackColor1 { get; set; }
        public Color BackColor2 { get; set; }
        public bool Flash { get; set; }
        public string Repeat { get; set; }
        public string Sound { get; set; }
    }
}
