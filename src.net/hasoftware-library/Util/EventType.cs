using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public enum EventType
    {
        [Description("None")]
        None = 0,
        [Description("Timecheck")]
        TimeCheck = 1,
        [Description("Shutdown")]
        Shutdown = 2,
        [Description("Connect")]
        Connect = 3,
        [Description("Disconnect")]
        Disconnect = 4,
        [Description("ClientConnect")]
        ClientConnect = 5,
        [Description("ClientDisconnect")]
        ClientDisconnect = 6,
        [Description("SendMessage")]
        SendMessage = 7,
        [Description("ReceiveMessage")]
        ReceiveMessage = 8
    }
}
