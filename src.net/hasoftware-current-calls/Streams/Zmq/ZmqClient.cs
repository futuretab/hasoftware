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

      private readonly SubscriberSocket _subscriber;
      private readonly Thread _subscriberThread;
      private bool _running;

      public ZmqClient(string subscribeTo)
      {
         _subscriber = new SubscriberSocket();
         _subscriber.Options.Identity = Encoding.UTF8.GetBytes(RandomUtils.RandomString(20));
         _subscriber.Connect(subscribeTo); // E.g. "tcp://10.2.0.69:5556"
         _subscriberThread = new Thread(() => SubscriberProcess());
      }

      public bool StartUp()
      {
         _running = true;
         _subscriberThread.Start();
         return true;
      }

      public bool ShutDown()
      {
         _running = false;
         _subscriberThread.Join();
         _subscriber.Close();
         return true;
      }

      public void Subscribe(string topic)
      {
         _subscriber.Subscribe(Encoding.UTF8.GetBytes(topic));
      }

      private void SubscriberProcess()
      {
         var message = new NetMQMessage();
         while (_running)
         {
            if (_subscriber.TryReceiveMultipartMessage(TimeSpan.FromMilliseconds(100), ref message))
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
