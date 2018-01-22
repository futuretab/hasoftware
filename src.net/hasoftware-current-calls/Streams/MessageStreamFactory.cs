using hasoftware.Configuration;
using hasoftware_current_calls.Streams.Rabbit;
using hasoftware_current_calls.Streams.Tap;
using hasoftware_current_calls.Streams.Zmq;

namespace hasoftware_current_calls.Streams
{
   public static class MessageStreamFactory
   {
      public static IMessageStream Create(XmlSettings settings)
      {
         if (settings == null)
         {
            return null;
         }
         var type = settings.GetString("type", null);
         if (type == null)
         {
            return null;
         }
         switch (type)
         {
            case "cdef":
               return new CdefMessageStream(settings);

            case "tap":
               return new TapMessageStream(settings);

            case "zmq":
               return new ZmqMessageStream(settings);

            case "rabbit":
               return new RabbitMessageStream(settings);
         }
         return null;
      }
   }
}
