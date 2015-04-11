using System;
using System.Text;
using System.Threading;
using NLog;
using NetMQ;
using NetMQ.Sockets;
using hasoftware.Util;

namespace hasoftware_current_calls.Streams.Zmq
{
    public class ZmqClient
    {
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();

        public delegate void OnZMQMessageEvent(string header, NetMQFrame frame);

        public event OnZMQMessageEvent OnZMQMessage;

        private readonly NetMQContext _context;
        private readonly SubscriberSocket _subscriber;
        private readonly Thread _subscriberThread;
        private bool _running;

        public ZmqClient(string subscribeTo)
        {
            _context = NetMQContext.Create();

            _subscriber = _context.CreateSubscriberSocket();
            _subscriber.Options.Identity = Encoding.UTF8.GetBytes(RandomUtils.RandomString(20));
            _subscriber.Connect("tcp://10.2.0.69:5556");
            _subscriberThread = new Thread(() => SubscriberProcess());
        }

        public bool StartUp() {
            _running = true;
            _subscriberThread.Start();
            return true;
        }

        public bool ShutDown()
        {
            _running = false;
            _subscriberThread.Join();
            _subscriber.Close();
            _context.Dispose();
            return true;
        }

        public void Subscribe(string topic)
        {
            _subscriber.Subscribe(Encoding.UTF8.GetBytes(topic));
        }

        private void SubscriberProcess()
        {
            while (_running)
            {
                var message = _subscriber.ReceiveMessage(TimeSpan.FromMilliseconds(100));
                if (message != null && _running)
                {
                    if (OnZMQMessage != null)
                    {
                        var header = message[0].ConvertToString();
                        var frame = message[1];
                        OnZMQMessage.Invoke(header, frame);
                    }
                }
            }
        }
    }
}
