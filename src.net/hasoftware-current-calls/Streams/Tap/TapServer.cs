using NLog;
using System;
using System.Net;
using System.Net.Sockets;

namespace hasoftware_current_calls.Streams.Tap
{
    public class TapServer
    {
        private static Logger logger = LogManager.GetCurrentClassLogger();

        private const int DefaultPort = 6970;

        private TcpListener _server;

        public int Port { get; set; }

        public TapServer()
        {
            logger.Debug("Creating TapServer");
            Port = 6970;
        }

        public bool StartUp()
        {
            if (_server == null)
            {
                _server = new TcpListener(IPAddress.Any, Port);
                _server.Start();
                logger.Debug("Listening on port {0}", Port);
            }
            return true;
        }

        public bool CanAccept()
        {
            return _server.Pending();
        }

        public TapClient Accept()
        {
            if (_server.Pending())
            {
                return new TapClient(_server.AcceptTcpClient());
            }
            return null;
        }
    }
}
