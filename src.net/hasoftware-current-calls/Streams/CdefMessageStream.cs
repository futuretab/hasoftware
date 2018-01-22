using hasoftware.Cdef;
using hasoftware.Configuration;
using hasoftware.Util;
using NLog;
using System.Collections.Concurrent;

namespace hasoftware_current_calls.Streams
{
   public class CdefMessageStream : IMessageStream
   {
      private static Logger logger = LogManager.GetCurrentClassLogger();

      private string _name;
      private CdefClient _client;

      public CdefMessageStream(XmlSettings settings)
      {
         _name = settings.GetString("name", "CDEF");
         var host = settings.GetString("host", "localhost");
         var port = settings.GetInt("port", 6969);
         _client = new CdefClient(host, port);
         logger.Debug("CDEF server address [{0}:{1}]", host, port);
      }

      public string Name()
      {
         return _name;
      }

      public bool SetEventQueue(ConcurrentQueue<Event> eventQueue)
      {
         return _client.SetEventQueue(eventQueue);
      }

      public bool StartUp()
      {
         return _client.StartUp();
      }

      public bool ReadyToShutDown()
      {
         return _client.ReadyToShutDown();
      }

      public bool ShutDown()
      {
         return _client.ShutDown();
      }

      public bool HandleEvent(Event e)
      {
         return _client.HandleEvent(e);
      }
   }
}
