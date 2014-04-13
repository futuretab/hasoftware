using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public interface IEventHandler
    {
        bool HandleEvent(Event e);
    }
}
