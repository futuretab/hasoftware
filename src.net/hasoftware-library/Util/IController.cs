using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace hasoftware.Util
{
    public interface IController
    {
        bool StartUp();

        bool ReadyToShutDown();

        bool ShutDown();
    }
}
