using System;
using System.Collections.Concurrent;
using hasoftware.Util;
using NLog;
using hasoftware.Configuration;
using Newtonsoft.Json;
using hasoftware.Messages;
using hasoftware.Cdef;
using hasoftware.Classes;
using hasoftware.Api;

namespace hasoftware_current_calls.Streams.Rabbit
{
   public class RabbitMessageStream : IMessageStream
   {
      private static readonly Logger Logger = LogManager.GetCurrentClassLogger();

      private string _name;
      private ConcurrentQueue<Event> _eventQueue;
      private readonly RabbitClient _rabbitClient;

      public RabbitMessageStream(XmlSettings settings)
      {
         Logger.Debug("Creating RabbitMessageStream");
         _name = settings.GetString("name", "Rabbit");
         _rabbitClient = new RabbitClient(
            settings.GetString("host", "localhost"),
            settings.GetString("vhost", "/"),
            settings.GetString("username", "hasoftware"),
            settings.GetString("password", "hasoftware"),
            settings.GetString("exchange", "hasoftware"),
            settings.GetString("routingKeys", ""));
         _rabbitClient.OnRabbitMessage += OnRabbitMessage;
         //_mappedCalls = new Dictionary<string, int>();
         //_nextId = 0;
      }

      public string Name()
      {
         return _name;
      }

      public bool StartUp()
      {
         _rabbitClient.StartUp();
         return true;
      }

      public bool ReadyToShutDown()
      {
         return true;
      }

      public bool ShutDown()
      {
         _rabbitClient.ShutDown();
         return true;
      }

      public bool SetEventQueue(ConcurrentQueue<Event> eventQueue)
      {
         _eventQueue = eventQueue;
         return true;
      }

      public bool HandleEvent(Event e)
      {
         return true;
      }

      private void OnRabbitMessage(string topic, byte[] frame)
      {
         string body = System.Text.Encoding.Default.GetString(frame);
         if (topic == "ADSB")
         {
            // PLANE,ICAO,FLIGHT,SQUAWK,ALTITUDE(m),SPEED(km/h),HEADING(degrees),LATITUDE,LONGITUDE
            // PLANE,7C79B8,YBU,1200,830.770750,226.129200,325.000000,-34.588592,138.406792

         }
         else if (topic == "/messages/incoming/can")
         {
            Logger.Info("OnRabbitMessage({0}, {1})", topic, body);
            dynamic message = JsonConvert.DeserializeObject(body);
            string type = message.type;
            if (type == "CALL" || type == "CANCEL")
            {
               HandleAlarmsMessage(type, (string)message.id);
            }
            else if (type == "ANALOG")
            {
               HandleAnalogMessage((string)message.id, (int)message.value);
            }
         }
      }

      private void HandleAnalogMessage(string id, int value)
      {
         var response = new CurrentEventResponse(0);
         var currentEvent = new CurrentEvent();
         currentEvent.Point = new Point();
         currentEvent.Point.Address = id;
         currentEvent.Point.Message1 = "" + value;
         currentEvent.Point.DeviceTypeCode = "TEMP";
         currentEvent.CreatedOn = DateTime.Now.CurrentTimeMillis();
         response.CurrentEvents.Add(currentEvent);
         _eventQueue.Enqueue(new Event(EventType.ReceiveMessage) { Message = response });
      }

      private void HandleAlarmsMessage(string type, string id)
      {
         if (type == "CALL")
         {
            var response = new CurrentEventResponse(0);
            var currentEvent = new CurrentEvent();
            currentEvent.Id = Int32.Parse(id);
            currentEvent.Point = new Point();
            currentEvent.Point.Id = Int32.Parse(id);
            currentEvent.Point.Priority = 10;
            currentEvent.Point.DeviceTypeCode = "POINT";
            currentEvent.CreatedOn = DateTime.Now.CurrentTimeMillis();
            switch (currentEvent.Point.Id)
            {
               case 1:
                  currentEvent.Point.Message1 = "OFFICE";
                  currentEvent.Point.Message2 = "sounds/back-office.wav";
                  break;
               case 2: currentEvent.Point.Message1 = "BEDROOM"; break;
               case 3:
                  currentEvent.Point.Message1 = "LOUNGE";
                  currentEvent.Point.Message2 = "sounds/lounge-room.wav";
                  break;
               case 4:
                  currentEvent.Point.Message1 = "FAMILY";
                  currentEvent.Point.Message2 = "sounds/family-room.wav";
                  break;
               case 5:
                  currentEvent.Point.Message1 = "THEATRE";
                  currentEvent.Point.Message2 = "sounds/home-theatre.wav";
                  break;
               case 6: currentEvent.Point.Message1 = "HALLWAY"; break;
               case 7:
                  currentEvent.Point.Message1 = "GARAGE";
                  currentEvent.Point.Message2 = "sounds/garage.wav";
                  break;
               case 9:
                  currentEvent.Point.Message1 = "CASE TAMPER";
                  currentEvent.Point.Priority = 3;
                  break;
               case 10:
                  currentEvent.Point.Message1 = "DOOR BELL";
                  currentEvent.Point.Priority = 0;
                  break;
               default:
                  currentEvent.Point.Message1 = "? POINT " + id;
                  currentEvent.Point.Priority = 8;
                  break;
            }
            response.CurrentEvents.Add(currentEvent);
            _eventQueue.Enqueue(new Event(EventType.ReceiveMessage) { Message = response });
         }
         else if (type == "CANCEL") {
            var response = new NotifyResponse(0);
            response.Action = CdefAction.Delete;
            response.NotifyFunctionCode = FunctionCode.CurrentEvent;
            response.Ids.Add(Int32.Parse(id));
            _eventQueue.Enqueue(new Event(EventType.ReceiveMessage) { Message = response });
         }
      }
   }
}
