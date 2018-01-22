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
        public string Id { get; set; }

        public bool RTUpdated { get; set; }
        public int RTTimer { get; set; }
    }
}
