﻿using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public interface IEventCreator
    {
        bool SetEventQueue(ConcurrentQueue<Event> eventQueue);
    }
}
