using hasoftware.Api;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public class Event
    {
        public EventType Type { get; private set; }
        public long Time { get; set; }
        public Message Message { get; set; }

        public Event(EventType type) 
            : this(type, null)
        {
        }

        public Event(EventType type, Message message) {
            Type = type;
            Message = message;
            Time = DateTime.Now.CurrentTimeMillis();
        }
    }
}
