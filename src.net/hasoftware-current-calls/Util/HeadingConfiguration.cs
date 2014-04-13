using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace hasoftware_current_calls.Util
{
    public class HeadingConfiguration
    {
        public string Type { get; set; }
        public string Format { get; set; }
        public string Filename { get; set; }
        public int Refresh { get; set; }
        public Label Label { get; set; }

        public bool RTUpdated { get; set; }
        public int RTTimer { get; set; }
    }
}
