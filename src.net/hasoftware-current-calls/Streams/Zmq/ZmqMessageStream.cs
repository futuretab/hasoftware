using System;
using System.Collections.Concurrent;
using NLog;
using NetMQ;
using NetMQ.Sockets;
using hasoftware.Configuration;
using hasoftware.Util;
using System.Collections.Generic;
using hasoftware.Messages;
using hasoftware.Cdef;
using hasoftware.Classes;
using hasoftware.Api;

namespace hasoftware_current_calls.Streams.Zmq
{
   public class ZmqMessageStream : IMessageStream
   {
      private static readonly Logger Logger = LogManager.GetCurrentClassLogger();

      private string _name;
      private ConcurrentQueue<Event> _eventQueue;
      private readonly ZmqClient _zmqClient;
      //private SubscriberSocket _subscriber;
      private readonly string _topics;
      private readonly Dictionary<string, int> _mappedCalls;
      private int _nextId;

      public ZmqMessageStream(XmlSettings settings)
      {
         Logger.Debug("Creating ZmqMessageStream");
         _name = settings.GetString("name", "ZMQ");
         _zmqClient = new ZmqClient(settings.GetString("subscribe-to", "localhost"));
         _zmqClient.OnZMQMessage += OnZmqMessage;
         _topics = settings.GetString("topics", "");
         _mappedCalls = new Dictionary<string, int>();
         _nextId = 0;
      }

      public string Name()
      {
         return _name;
      }

      public bool StartUp()
      {
         _zmqClient.StartUp();
         foreach (var topic in _topics.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries))
         {
            Logger.Debug("Subscribing to {0}", topic);
            _zmqClient.Subscribe(topic);
         }
         return true;
      }

      public bool ReadyToShutDown()
      {
         return true;
      }

      public bool ShutDown()
      {
         _zmqClient.ShutDown();
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

      private void OnZmqMessage(string header, NetMQFrame frame)
      {
         if (header.StartsWith("ALARMS")) { HandleAlarmsMessage(header, frame); }
         if (header.StartsWith("")) { HandleMonitorMessage(header, frame); }
      }

      private void HandleMonitorMessage(string header, NetMQFrame frame)
      {
         // Channel update
         // MSG, 2, 0, 2, 0.000, SOUNDER, INACTIVE, 15.625
         var parts = frame.ConvertToString().Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries);
         for (var i = 0; i < parts.Length; i++)
         {
            parts[i] = parts[i].Trim();
         }
         if (parts[1] == "2" && parts.Length >= 8)
         {
            var call = (parts[6].ToUpper() != "INACTIVE");
            var id = string.Format("{0}", int.Parse(parts[2]) * 100 + int.Parse(parts[3]));
            var callId = -1;
            if (call)
            {
               var priority = (parts[6].ToUpper() == "HALT ON ERR") ? 2 : 6;
               var message = string.Format("RX:{0} CH:{1} {2} @ {3}Mhz", parts[2], parts[3], parts[5], parts[4]);
               if (_mappedCalls.ContainsKey(id))
               {
                  callId = _mappedCalls[id];
                  Logger.Error("Mapped alarm [{0}] already exists as [{1}] => {2}", id, callId, message);
               }
               else
               {
                  callId = _nextId++;
                  _mappedCalls.Add(id, callId);
               }
               var m = new CurrentEventResponse(0);
               m.Action = CdefAction.Create;
               m.CurrentEvents.Add(
                   new CurrentEvent
                   {
                      Id = callId,
                      CreatedOn = DateTime.Now.CurrentTimeMillis(),
                      Point = new Point
                      {
                         Priority = priority,
                         Message1 = message
                      }
                   });
               Logger.Debug("Adding call [{0}][{1}][{2}]", id, priority, message);
               _eventQueue.Enqueue(new Event(EventType.ReceiveMessage, m));
            }
            else
            {
               if (!_mappedCalls.ContainsKey(id))
               {
                  Logger.Error("Mapped alarm [{0}] does not exist", id);
                  return;
               }
               callId = _mappedCalls[id];
               _mappedCalls.Remove(id);
               var m = new NotifyResponse(0)
               {
                  Action = CdefAction.Delete,
                  NotifyFunctionCode = FunctionCode.CurrentEvent
               };
               m.Ids.Add(callId);
               Logger.Debug("Removing call [{0}]", callId);
               _eventQueue.Enqueue(new Event(EventType.ReceiveMessage, m));
            }
         }
      }

      private void HandleAlarmsMessage(string header, NetMQFrame frame)
      {
         // ADD,00-FF-FF-238C,4,DRX00 - Status Time:11:13:03,1427849062013
         // REMOVE,00-FF-FF-238C
         var parts = frame.ConvertToString().Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries);
         if (parts.Length >= 2)
         {
            var call = (parts[0].ToUpper() == "ADD");
            var id = parts[1];
            var callId = -1;
            if (call && parts.Length >= 5)
            {
               var priority = parts[2] == "4" ? 2 : 4;
               var message = parts[3];
               var created = long.Parse(parts[4]);
               if (_mappedCalls.ContainsKey(id))
               {
                  callId = _mappedCalls[id];
                  Logger.Error("Mapped alarm [{0}] already exists as [{1}] => {2}", id, callId, parts[3]);
               }
               else
               {
                  callId = _nextId++;
                  _mappedCalls.Add(id, callId);
               }
               var m = new CurrentEventResponse(0);
               m.Action = CdefAction.Create;
               m.CurrentEvents.Add(
                   new CurrentEvent
                   {
                      Id = callId,
                      CreatedOn = DateTime.Now.CurrentTimeMillis(),
                      Point = new Point
                      {
                         Priority = priority,
                         Message1 = message
                      }
                   });
               Logger.Debug("Adding call [{0}][{1}][{2}]", id, priority, message);
               _eventQueue.Enqueue(new Event(EventType.ReceiveMessage, m));
            }
            else
            {
               if (!_mappedCalls.ContainsKey(id))
               {
                  Logger.Error("Mapped alarm [{0}] does not exist", id);
                  return;
               }
               callId = _mappedCalls[id];
               _mappedCalls.Remove(id);
               var m = new NotifyResponse(0)
               {
                  Action = CdefAction.Delete,
                  NotifyFunctionCode = FunctionCode.CurrentEvent
               };
               m.Ids.Add(callId);
               Logger.Debug("Removing call [{0}]", callId);
               _eventQueue.Enqueue(new Event(EventType.ReceiveMessage, m));
            }
         }
      }
   }
}
