using hasoftware.Api;
using hasoftware.Cdef;
using hasoftware.Classes;
using hasoftware.Configuration;
using hasoftware.Messages;
using hasoftware.Util;
using NLog;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Threading;

namespace hasoftware_current_calls.Streams.Tap
{
   public class TapMessageStream : IMessageStream
   {
      private static readonly Logger Logger = LogManager.GetCurrentClassLogger();

      private const string TAP_SERVER_REQUEST_FOR_ID = "ID=";
      private const string TAP_SERVER_AUTOMATIC_MODE = "\x1BPG1";
      private const string TAP_SERVER_MANUAL_MODE = "M";
      private const string TAP_SERVER_VERSION = "110 1.7";
      private const string TAP_SERVER_WELCOME = "114 Thank you for calling the TAP server";
      private const string TAP_SERVER_GO_AHEAD = "\x1B[p";
      private const string TAP_SERVER_PAGE_ACCEPTED = "211 Page accepted";
      private const string TAP_SERVER_ACK = "\x06";
      private const string TAP_SERVER_QUIT = "QUIT";
      private const string TAP_SERVER_RESET = "+++";
      private const string TAP_SERVER_NACK = "\0x15";
      private const string TAP_SERVER_GOODBYE = "115 Goodbye";
      private const string TAP_SERVER_CHECKSUM_ERROR = "514 Checksum error";
      private const string TAP_ENTER_PAGER_ID = "114 Enter cap code:";
      private const string TAP_ENTER_PAGER_MESSAGE = "114 Enter message :";
      private const string TAP_SERVER_ESC_EOT = "\x1B\x04";
      private const string TAP_CAP_CODE_ERROR = "510 Invalid cap code";

      private const int DefaultDisplayId = 1111;

      private enum State
      {
         Idle,
         WaitingForClient,
         WaitingForCR,
         WaitForLogin,
         WaitMessageAutomatic,
         WaitMessageManual,
         ThatsIt
      };

      private enum FieldState
      {
         CapCode,
         Message
      }

      private string _name;
      private ConcurrentQueue<Event> _eventQueue;
      private State _state;
      private readonly TapServer _server;
      private TapClient _client;
      private FieldState _fieldState;
      private int _pagerCapCode;
      private string _pagerMessage;
      private int _checksum;
      private Thread _thread;
      private bool _running;
      private readonly int _displayId;
      private int _nextId;
      private readonly Dictionary<string, int> _mappedCalls;

      public TapMessageStream(XmlSettings settings)
      {
         Logger.Debug("Creating TapMessageStream");
         _name = settings.GetString("name", "TAP");
         _server = new TapServer();
         _server.Port = settings.GetInt("port", _server.Port);
         _displayId = settings.GetInt("display-id", DefaultDisplayId);
         _nextId = 1;
         _mappedCalls = new Dictionary<string, int>();
      }

      public string Name()
      {
         return _name;
      }

      public bool SetEventQueue(ConcurrentQueue<Event> eventQueue)
      {
         _eventQueue = eventQueue;
         return true;
      }

      public bool StartUp()
      {
         if (_thread == null)
         {
            _running = true;
            _thread = new Thread(Run);
            _thread.Start();
         }
         return true;
      }

      public bool ReadyToShutDown()
      {
         return true;
      }

      public bool ShutDown()
      {
         if (_thread != null)
         {
            _running = false;
            _thread.Join();
         }
         return true;
      }

      public bool HandleEvent(Event e)
      {
         return true;
      }

      private void Run()
      {
         while (_running)
         {
            StateMachine();
            Thread.Sleep(100);
         }
      }

      private void StateMachine()
      {
         string message;

         if (_client != null && !_client.Connected)
         {
            _state = State.ThatsIt;
         }

         switch (_state)
         {
            case State.Idle:
               _server.StartUp();
               _state = State.WaitingForClient;
               break;

            case State.WaitingForClient:
               if (_server.CanAccept() == true)
               {
                  _client = _server.Accept();
                  _state = State.WaitingForCR;
               }
               break;

            case State.WaitingForCR:
               if (_client.WaitFor("") == true)
               {
                  _client.Send(TAP_SERVER_REQUEST_FOR_ID);
                  Logger.Debug("*** SENT REQUEST FOR ID ***");
                  _state = State.WaitForLogin;
               }
               break;

            case State.WaitForLogin:
               if (_client.Receive(out message) == true)
               {
                  if (message.IndexOf(TAP_SERVER_AUTOMATIC_MODE) != -1)
                  {
                     Logger.Debug("*** ENTERING AUTOMATIC MODE ***");
                     _client.Send(TAP_SERVER_VERSION);
                     _client.Send(TAP_SERVER_WELCOME);
                     Logger.Debug("*** SENT WELCOME ***");
                     _client.Send(TAP_SERVER_ACK);
                     Logger.Debug("*** SENT ACK ***");
                     _client.Send(TAP_SERVER_GO_AHEAD);
                     Logger.Debug("*** SENT GO AHEAD ***");
                     _state = State.WaitMessageAutomatic;
                  }
                  else if (message.Length == 1 && message.StartsWith(TAP_SERVER_MANUAL_MODE))
                  {
                     Logger.Debug("*** ENTERING MANUAL MODE ***");
                     _client.Send(TAP_SERVER_VERSION, true);
                     _client.Send(TAP_SERVER_WELCOME, true);
                     Logger.Debug("*** SENT WELCOME ***");
                     _client.Send(TAP_ENTER_PAGER_ID);
                     _state = State.WaitMessageManual;
                     _fieldState = FieldState.CapCode;
                  }
               }
               break;

            case State.WaitMessageManual:
               if (_client.Receive(out message) == true)
               {
                  Logger.Debug("*** Read [{0}] ***", message);
                  if (message.IndexOf(TAP_SERVER_QUIT) != -1 || message.IndexOf(TAP_SERVER_RESET) != -1)
                  {
                     _client.Send(TAP_SERVER_GOODBYE);
                     _client.Send(TAP_SERVER_ESC_EOT);
                     _state = State.ThatsIt;
                  }
                  else
                  {
                     switch (_fieldState)
                     {
                        case FieldState.CapCode:
                           if (int.TryParse(message, out _pagerCapCode))
                           {
                              _fieldState = FieldState.Message;
                              _client.Send(TAP_ENTER_PAGER_MESSAGE);
                           }
                           else
                           {
                              _client.Send(TAP_CAP_CODE_ERROR, true);
                              _client.Send(TAP_ENTER_PAGER_ID);
                           }
                           break;

                        case FieldState.Message:
                           _pagerMessage = message;
                           HandleDecodedMessage();
                           _client.Send(TAP_SERVER_PAGE_ACCEPTED, true);
                           _fieldState = FieldState.CapCode;
                           _client.Send(TAP_ENTER_PAGER_ID);
                           break;
                     }
                  }
               }
               break;

            case State.WaitMessageAutomatic:
               if (_client.Receive(out message) == true)
               {
                  Logger.Debug("*** Read [{0}] ***", message);
                  if (message.Length >= 1 && message[0] == Serial.EOT)
                  {
                     _client.Send(TAP_SERVER_GOODBYE);
                     _client.Send(TAP_SERVER_ESC_EOT);
                     _state = State.ThatsIt;
                  }
                  if (message.Length >= 1 && message[0] == Serial.ETX)
                  {
                     PrintChecksum(Serial.ETX, ref _checksum);
                     if ((message[1] == (((_checksum >> 8) & 0x0F) + 0x30)) &&
                         (message[2] == (((_checksum >> 4) & 0x0F) + 0x30)) &&
                         (message[3] == (((_checksum >> 0) & 0x0F) + 0x30)))
                     {
                        _client.Send(TAP_SERVER_PAGE_ACCEPTED);
                        Logger.Debug("*** SENT PAGE ACCEPTED ***");
                        _client.Send(TAP_SERVER_ACK);
                        Logger.Debug("*** SENT ACK ***");
                        HandleDecodedMessage();
                     }
                     else
                     {
                        _client.Send(TAP_SERVER_CHECKSUM_ERROR);
                        Logger.Debug("*** SENT CHECKSUM ERROR ***");
                        _client.Send(TAP_SERVER_NACK);
                        Logger.Debug("*** SENT NACK ***");
                     }
                  }
                  else
                  {
                     if (message.Length >= 1 && message[0] == Serial.STX)
                     {
                        _fieldState = FieldState.CapCode;
                        _checksum = 0;
                     }
                     for (var i = 0; i < message.Length; i++)
                     {
                        PrintChecksum((byte)(message[i] & 0x07F), ref _checksum);
                     }
                     PrintChecksum(Serial.CR, ref _checksum);
                     switch (_fieldState)
                     {
                        case FieldState.CapCode:
                           _pagerCapCode = int.Parse(message.Substring(1));
                           _fieldState = FieldState.Message;
                           break;

                        case FieldState.Message:
                           _pagerMessage = message;
                           break;
                     }
                  }
               }
               break;

            case State.ThatsIt:
               Logger.Debug("*** CLIENT DISCONNECTED ***");
               _client.Close();
               _client = null;
               _state = State.WaitingForClient;
               break;
         }
      }

      private void PrintChecksum(byte b, ref int checksum)
      {
         checksum = checksum + (b & 0x7F);

         string debug;
         switch (b)
         {
            case Serial.STX:
               debug = string.Format("<STX>  {0} {1}", b, checksum);
               break;

            case 0x03:
               debug = string.Format("<ETX>  {0} {1}", b, checksum);
               break;

            case 0x04:
               debug = string.Format("<EOT>  {0} {1}", b, checksum);
               break;

            case 0x0D:
               debug = string.Format("<CR>   {0} {1}", b, checksum);
               break;

            default:
               debug = string.Format("     {0} {0} {1}", (char)b, b, checksum);
               break;
         }
         Logger.Debug(debug);
      }

      private void HandleDecodedMessage()
      {
         Logger.Debug("Message received [{0}][{1}]", _pagerCapCode, _pagerMessage);
         if (_pagerCapCode != _displayId)
         {
            Logger.Error("Pager Cap Code {0} doesn't match display ID {1}", _pagerCapCode, _displayId);
            return;
         }
         var startMessage = _pagerMessage.IndexOf('[');
         if (startMessage == -1)
         {
            Logger.Error("Pager message [{0}] can't find start of message '['", _pagerMessage);
            return;
         }
         var endMessage = _pagerMessage.IndexOf(']', startMessage);
         if (endMessage == -1)
         {
            Logger.Error("Pager message [{0}] can't find end of message ']'", _pagerMessage);
            return;
         }

         // Determine message
         var message = _pagerMessage.Substring(startMessage + 1, (endMessage - startMessage - 1));
         if (message.Length == 0)
         {
            Logger.Error("Pager message [{0}] zero length", _pagerMessage);
            return;
         }

         // Determine priority
         var types = _pagerMessage.Substring(endMessage + 1).Split(new[] { ' ' }, 1);
         if (types == null || types.Length == 0)
         {
            Logger.Error("Pager message [{0}] can't extract type", _pagerMessage);
            return;
         }
         var type = types[0].ToUpper().Trim();
         var priority = 8;
         switch (type)
         {
            case "EMERGENCY":
               priority = 2;
               break;
            case "STAFF":
               priority = 4;
               break;
            case "CALL":
               priority = 5;
               break;
            case "NURSE":
               priority = 6;
               break;
         }

         // Determine call or cancel
         var call = true;
         if (message.StartsWith("cancelled: "))
         {
            call = false;
            message = message.Substring(11);
         }

         var mappedMessage = message + type;

         // Construct pretend message & post into event queue
         if (call)
         {
            if (_mappedCalls.ContainsKey(mappedMessage))
            {
               var mappedCallId = _mappedCalls[mappedMessage];
               Logger.Error("Mapped message [{0}] already exists [{1}]", mappedMessage, mappedCallId);
               return;
            }
            var id = _nextId++;
            _mappedCalls.Add(mappedMessage, id);
            var m = new CurrentEventResponse(0);
            m.Action = CdefAction.Create;
            m.CurrentEvents.Add(
                new CurrentEvent
                {
                   Id = id,
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
            if (!_mappedCalls.ContainsKey(mappedMessage))
            {
               Logger.Error("Mapped message [{0}] does not exist", mappedMessage);
               return;
            }
            var mappedCallId = _mappedCalls[mappedMessage];
            _mappedCalls.Remove(mappedMessage);
            var m = new NotifyResponse(0)
            {
               Action = CdefAction.Delete,
               NotifyFunctionCode = FunctionCode.CurrentEvent
            };
            m.Ids.Add(mappedCallId);
            Logger.Debug("Removing call [{0}][{1}][{2}]", mappedCallId, priority, message);
            _eventQueue.Enqueue(new Event(EventType.ReceiveMessage, m));
         }

         // 5555 = display ID
         // [room 26] = message
         // Emergency/call = priority
         // #4 = zone 9
         // extended message
         // Keywords to set priority = EMERGENCY, STAFF Assist, CALL, NURSE Presence

         // [Room 26] CALL #4 Nurse Call
         // [Room 26] EMERGENCY #4 Nurse Call
         // [Room 26] CALL #5 Bed

         // [cancelled: Room 26] CALL #4 Nurse Call
         // [Room 26] EMERGENCY #4 Nurse Call
         // [Room 26] CALL #5 Bed
      }
   }
}
