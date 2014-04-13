using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware_current_calls.Util
{
    public class Utils
    {
        public static ContentAlignment GetTextAlignment(string data)
        {
            data = data.ToUpper();
            switch (data)
            {
                case "L":
                case "LEFT":
                    return ContentAlignment.MiddleLeft;

                case "R":
                case "RIGHT":
                    return ContentAlignment.MiddleRight;

                default:
                    return ContentAlignment.MiddleCenter;
            }
        }
    }
}
