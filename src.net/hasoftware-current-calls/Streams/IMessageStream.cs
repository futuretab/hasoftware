using hasoftware.Util;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware_current_calls.Streams
{
    public interface IMessageStream : IController, IEventCreator, IEventHandler
    {
    }
}
