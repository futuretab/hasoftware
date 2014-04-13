using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace hasoftware_current_calls.Util
{
    public class ColumnConfiguration
    {
        public int Offset { get; set; }
        public int Width { get; set; }
        public string Format { get; set; }
        public ContentAlignment Align { get; set; }
        public string Type { get; set; }
        public Font Font { get; set; }
        public Label[] Labels { get; set; }
    }
}
