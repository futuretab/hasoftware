using AltarNet;
using hasoftware.Api;
using hasoftware.Util;
using NLog;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Cdef
{
    public class CdefClient : AbstractController
    {
        private static Logger logger = LogManager.GetCurrentClassLogger();

        private const int ConnectDelay = 2;

        private TcpClientHandler _client;
        private ConcurrentQueue<Event> _eventQueue;
        private List<Message> _messageQueue;
        private string _host;
        private int _port;
        private bool _connected;

        public CdefClient(string host, int port)
        {
            _host = host.Trim();
            _port = port;
            _messageQueue = new List<Message>();
            _connected = false;
        }

        public override bool StartUp()
        {
            logger.Debug("startUp");
            if (_eventQueue == null)
            {
                logger.Error("EventQueue not set");
                return false;
            }
            var ipAddress = Dns.GetHostEntry(_host).AddressList.First(ip => ip.AddressFamily == AddressFamily.InterNetwork);
            if (ipAddress == null)
            {
                logger.Error("Invalid host name [{0}]", _host);
                return false;
            }
            _client = new TcpClientHandler(ipAddress, _port);
            _client.Disconnected += OnDisconnected;
            //_client.ReceivedFragment += OnReceivedFragment;
            _client.ReceivedFull += OnReceivedFull;
            Task.Delay(ConnectDelay * 1000).ContinueWith(t => Connect());
            return true;
        }

        public override bool ReadyToShutDown()
        {
            logger.Debug("readyToShutDown");
            return true;
        }

        public override bool ShutDown()
        {
            logger.Debug("shutDown");
            _client.Disconnect();
            return true;
        }

        public override bool SetEventQueue(ConcurrentQueue<Event> eventQueue)
        {
            logger.Debug("setEventQueue");
            _eventQueue = eventQueue;
            return true;
        }

        public override bool HandleEvent(Event e)
        {
            switch (e.Type)
            {
                case EventType.TimeCheck:
                    break;

                case EventType.Connect:
                    _connected = true;
                    TrySendingMessages();
                    break;

                case EventType.SendMessage:
                    _messageQueue.Add(e.Message);
                    TrySendingMessages();
                    break;

                case EventType.Disconnect:
                    Task.Delay(ConnectDelay * 1000).ContinueWith(t => Connect());
                    break;
            }
            return true;
        }

        private bool Connect()
        {
            logger.Debug("Connecting to server {}:{}", _host, _port);
            if (_client.Connect())
            {
                _connected = true;
                _eventQueue.Enqueue(new Event(EventType.Connect));
            }
            else
            {
                logger.Error("Connect error - {0}", _client.LastConnectError.Message);
                Task.Delay(ConnectDelay * 1000).ContinueWith(t => Connect());
            }
            return true;
        }

        private void OnDisconnected(object sender, TcpEventArgs e)
        {
            _connected = false;
            _eventQueue.Enqueue(new Event(EventType.Disconnect));
        }

        private void OnReceivedFragment(object sender, TcpFragmentReceivedEventArgs e)
        {
            logger.Debug("OnReceivedFragment");
        }

        private void OnReceivedFull(object sender, TcpReceivedEventArgs e)
        {
            logger.Debug("OnReceivedFull");
            var message = MessageFactory.Decode(new CdefMessage(e.Data, e.Data.Length));
            _eventQueue.Enqueue(new Event(EventType.ReceiveMessage, message));
        }

        private void TrySendingMessages()
        {
            if (_connected)
            {
                while (_messageQueue.Count != 0)
                {
                    var message = _messageQueue[0];
                    var cdefMessage = new CdefMessage();
                    message.Encode(cdefMessage);
                    _client.Send(cdefMessage.Bytes, 0, cdefMessage.Length, true);
                    _messageQueue.Remove(_messageQueue[0]);
                }
            }
        }
    }
}
