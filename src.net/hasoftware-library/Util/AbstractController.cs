using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public abstract class AbstractController : IController, IEventCreator, IEventHandler
    {
        public abstract bool StartUp();
        public abstract bool ReadyToShutDown();
        public abstract bool ShutDown();
        public abstract bool SetEventQueue(ConcurrentQueue<Event> eventQueue);
        public abstract bool HandleEvent(Event e);
    }
}
