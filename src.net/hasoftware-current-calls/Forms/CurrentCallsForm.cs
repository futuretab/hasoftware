using hasoftware.Api;
using hasoftware.Cdef;
using hasoftware.Classes;
using hasoftware.Configuration;
using hasoftware.Messages;
using hasoftware.Util;
using hasoftware_current_calls.Streams;
using hasoftware_current_calls.Util;
using NLog;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Media;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace hasoftware_current_calls.Forms
{
   public partial class CurrentCallsForm : Form
   {
      private static readonly Logger Logger = LogManager.GetCurrentClassLogger();

      private const string XmlConfigurationFile = "current-calls.xml";
      private const int TimeCheckPeriod = 1000;
      private const string FormatElapsedTime = "$E";

      private XmlConfiguration _xmlConfiguration;
      private readonly SoundPlayer _soundPlayer;
      private string _fontName;
      private float _fontSize;
      private Font _font;
      private List<HeadingConfiguration> _headings;
      private Dictionary<String, IMessageStream> _messageStreams;
      private readonly ConcurrentQueue<Event> _eventQueue;
      private readonly Event _timeCheckEvent;
      private string _status;
      private int _rowCount;
      private List<ColumnConfiguration> _columns;
      private readonly List<CurrentEventData> _currentEvents;
      private readonly CurrentEventDataSorter _currentEventDataSorter;
      private DateTime _currentEventsUpdatedTime;
      private DateTime _currentEventsDisplayedTime;
      private bool _newCurrentEventDisplayedFlag;
      private CurrentEventData[] _currentEventsDisplayed;
      private bool _flashFlag;
      private int _flashCounter;
      private List<PriorityData> _priorities;
      private CurrentEventData _lastSoundEvent;
      private bool _soundTimerFlag;
      private int _soundTimer;
      private Stream _soundStream;
      private uint _soundVolume;
      private Task _task;

      public CurrentCallsForm()
      {
         InitializeComponent();
         _soundPlayer = new SoundPlayer();
         _eventQueue = new ConcurrentQueue<Event>();
         _timeCheckEvent = new Event(EventType.TimeCheck);
         _currentEvents = new List<CurrentEventData>();
         _currentEventDataSorter = new CurrentEventDataSorter();
         _currentEventsUpdatedTime = DateTime.MinValue;
         _newCurrentEventDisplayedFlag = false;
         _currentEventsDisplayedTime = DateTime.MinValue;
         _flashFlag = false;
         _flashCounter = 0;
         _lastSoundEvent = null;
         _soundTimerFlag = false;
         _soundTimer = 0;
         _soundStream = null;
         _messageStreams = new Dictionary<string, IMessageStream>();
         LoadSettings();
      }

      private void LoadSettings()
      {
         Logger.Debug("-- LOAD SETTINGS START --");
         SuspendLayout();
         _xmlConfiguration = new XmlConfiguration();
         if (!_xmlConfiguration.Load(XmlConfigurationFile))
         {
            MessageBox.Show("Can not load the configuration file: " + XmlConfigurationFile, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
         }
         Options.Load(_xmlConfiguration);
         using (_xmlConfiguration.XmlBuffer)
         {
            Logger.Debug("-- LOAD COMMS SETTINGS --");
            {
               for (var streamNumber = 0; streamNumber < 10; streamNumber++)
               {
                  var messageStream = MessageStreamFactory.Create(_xmlConfiguration.GetSettings("configuration/comms/stream" + streamNumber));
                  if (messageStream != null)
                  {
                     messageStream.SetEventQueue(_eventQueue);
                     _messageStreams.Add(messageStream.Name(), messageStream);
                  }
               }
               _status = "Created " + _messageStreams.Count + " streams";
            }


            Logger.Debug("-- LOAD FONT SETTINGS --");
            {
               var settings = _xmlConfiguration.GetSettings("configuration/display/font");
               _fontName = settings.GetString("name", "Tahoma");
               _fontSize = settings.GetFloat("size", 28);
               _font = new Font(_fontName, _fontSize);
               Logger.Debug("DEFAULT FONT [{0} {1}]", _fontName, _fontSize);
            }

            Logger.Debug("-- LOAD DISPLAY SETTINGS --");
            {
               var screenCount = 0;
               foreach (var screen in Screen.AllScreens)
               {
                  Logger.Debug(string.Format("[{0}] [{1}x{2}]@{3} {4}", screenCount++, screen.Bounds.Width, screen.Bounds.Height, screen.BitsPerPixel, screen.Primary));
               }
               var settings = _xmlConfiguration.GetSettings("configuration/display");
               var tempStr = settings.GetString("mode", "fullscreen").ToUpper();
               if (tempStr == "FULLSCREEN")
               {
                  var tempInt = settings.GetInt("screen", 0);
                  var screen = Screen.AllScreens[tempInt];
                  if (tempInt != 0 && Screen.AllScreens.Length >= (tempInt - 1))
                  {
                     StartPosition = FormStartPosition.Manual;
                     Bounds = screen.Bounds;
                  }
                  FormBorderStyle = FormBorderStyle.None;
                  ClientSize = new Size(screen.Bounds.Width, screen.Bounds.Height);
                  Cursor.Hide();
                  Logger.Debug("FULLSCREEN MODE ON SCREEN {0}", tempInt);
               }
               else
               {
                  var width = settings.GetInt("width", 1024);
                  var height = settings.GetInt("height", 768);
                  ClientSize = new Size(width, height);
                  FormBorderStyle = FormBorderStyle.FixedSingle;
                  MaximizeBox = false;
                  Logger.Debug("WINDOW MODE");
               }
            }

            Logger.Debug("-- LOAD BACKGROUND IMAGE --");
            {
               var settings = _xmlConfiguration.GetSettings("configuration/display/background");
               var filename = settings.GetString("filename", null);
               if (!string.IsNullOrEmpty(filename) && File.Exists(filename))
               {
                  BackgroundImage = Image.FromFile(filename);
                  Logger.Debug("USING IMAGE [{0}]", filename);
               }
               else
               {
                  var color = settings.GetString("back-color", "White");
                  BackColor = Color.FromName(color);
                  Logger.Debug("USING COLOR [{0}]", color);
               }
            }

            Logger.Debug("-- LOAD LABEL SETTINGS --");
            {
               _headings = new List<HeadingConfiguration>();
               var headingIndex = 0;
               while (true)
               {
                  var settings = _xmlConfiguration.GetSettings("configuration/labels/label" + headingIndex++);
                  if (settings == null)
                  {
                     break;
                  }
                  var hc = new HeadingConfiguration
                  {
                     Format = settings.GetString("format", ""),
                     Type = settings.GetString("type", "STATIC").ToUpper(),
                     Filename = settings.GetString("filename", null),
                     Refresh = settings.GetInt("refresh", 0),
                     RTTimer = int.MaxValue,
                     RTUpdated = false,
                     Id = settings.GetString("id", ""),
                     Label =
                          new Label
                          {
                             ForeColor = Color.FromName(settings.GetString("fore-color", "Black")),
                             BackColor = Color.FromName(settings.GetString("back-color", "Transparent")),
                             Location = new System.Drawing.Point(settings.GetInt("x", 0), settings.GetInt("y", 0)),
                             Size = new Size(settings.GetInt("width", 10), settings.GetInt("height", 10)),
                             TextAlign = Utils.GetTextAlignment(settings.GetString("align", "right")),
                             Text = ""
                          }
                  };
                  if (hc.Type == "STATIC")
                  {
                     hc.Label.Text = hc.Format;
                  }
                  var fontName = settings.GetString("font-name", _fontName);
                  var fontSize = settings.GetFloat("font-size", _fontSize);
                  if (fontName == _fontName && _fontSize == fontSize)
                  {
                     hc.Label.Font = _font;
                  }
                  else
                  {
                     hc.Label.Font = new Font(fontName, fontSize);
                  }
                  Controls.Add(hc.Label);
                  _headings.Add(hc);
               }
            }

            Logger.Debug("-- LOAD PRIORITIES --");
            {
               _priorities = new List<PriorityData>();
               var priorityIndex = 0;
               while (true)
               {
                  var settings = _xmlConfiguration.GetSettings("configuration/priorities/priority" + priorityIndex++);
                  if (settings == null || settings.Count == 0) break;
                  var configuration = settings.GetString("configuration", "");
                  var sound = settings.GetString("sound", null);
                  if (!File.Exists(sound)) sound = null;
                  if (string.IsNullOrEmpty(configuration)) continue;
                  var parts = configuration.Split(new[] { '|' }, StringSplitOptions.RemoveEmptyEntries);
                  if (parts.Length != 8) continue;
                  _priorities.Add(new PriorityData
                  {
                     // "0| DB |DOORBELL |Black |Lime   |Lime   |false |1"
                     Level = Int32.Parse(parts[0]),
                     Code = parts[1],
                     Description = parts[2],
                     ForeColor = Color.FromName(parts[3].Trim()),
                     BackColor1 = Color.FromName(parts[4].Trim()),
                     BackColor2 = Color.FromName(parts[5].Trim()),
                     Flash = bool.Parse(parts[6]),
                     Repeat = parts[7],
                     Sound = sound
                  });
               }
            }

            Logger.Debug("-- LOAD ROWS AND COLUMNS --");
            {
               _columns = new List<ColumnConfiguration>();
               var settings = _xmlConfiguration.GetSettings("configuration/rows");
               _rowCount = settings.GetInt("count", 10);
               _currentEventsDisplayed = new CurrentEventData[_rowCount];
               var height = settings.GetInt("height", 35);
               var offset = settings.GetInt("offset", 10);
               var y = settings.GetInt("y", 40);
               var columnIndex = 0;
               var fontName = _fontName;
               var fontSize = _fontSize;
               while (true)
               {
                  settings = _xmlConfiguration.GetSettings("configuration/rows/column" + columnIndex++);
                  if (settings == null)
                  {
                     break;
                  }
                  var cc = new ColumnConfiguration
                  {
                     Offset = settings.GetInt("offset", 0),
                     Width = settings.GetInt("width", 0),
                     Format = settings.GetString("format", ""),
                     Align = Utils.GetTextAlignment(settings.GetString("align", "right")),
                     Type = settings.GetString("type", ""),
                     Labels = new Label[_rowCount]
                  };

                  fontName = settings.GetString("font-name", _fontName);
                  fontSize = settings.GetFloat("font-size", _fontSize);
                  if (fontName == _fontName && _fontSize == fontSize)
                  {
                     cc.Font = _font;
                  }
                  else
                  {
                     cc.Font = new Font(fontName, fontSize);
                  }
                  _columns.Add(cc);
               }
               foreach (var column in _columns)
               {
                  var labelY = y;
                  for (var i = 0; i < column.Labels.Length; i++)
                  {
                     var label = new Label
                     {
                        Font = column.Font,
                        Location = new System.Drawing.Point(column.Offset, labelY),
                        Size = new Size(column.Width, height),
                        TextAlign = column.Align,
                        Visible = false
                     };
                     labelY += offset;
                     column.Labels[i] = label;
                     Controls.Add(label);
                  }
               }
            }
         }
         ResumeLayout();
         _task = Task.Run(() =>
         {
            PassEvents();
         });
         _timer.Start();
         Logger.Debug("-- LOAD SETTINGS STOP --");
      }

      private void PassEvents()
      {
         long lastTimeCheck = 0;

         foreach(IMessageStream messageStream in _messageStreams.Values)
         {
            messageStream.StartUp();
         }
         while (true)
         {
            Event e = null;
            var suceess = _eventQueue.TryDequeue(out e);
            if (e != null)
            {
               SendEvent(e);
               switch (e.Type)
               {
                  case EventType.Shutdown:
                     return;

                  case EventType.Disconnect:
                     _status = "Connecting ...";
                     break;

                  case EventType.Connect:
                     _status = "Connected";
                     _eventQueue.Enqueue(
                         new Event(EventType.SendMessage,
                             new LoginRequest { Username = "current-calls", Password = "current-calls" }));
                     break;

                  case EventType.ReceiveMessage:
                     switch (e.Message.FunctionCode)
                     {
                        case FunctionCode.Login:
                           HandleLoginResponse(e.Message);
                           break;
                        case FunctionCode.Notify:
                           HandleNotifyResponse(e.Message);
                           break;
                        case FunctionCode.CurrentEvent:
                           HandleCurrentEventResponse(e.Message);
                           break;
                     }
                     break;
               }
            }
            long now = DateTime.Now.CurrentTimeMillis();
            if ((now - lastTimeCheck) > TimeCheckPeriod)
            {
               lastTimeCheck = now;
               _timeCheckEvent.Time = now;
               SendEvent(_timeCheckEvent);
            }
            Thread.Sleep(200);
         }
      }

      private void AddCurrentEvent(CurrentEvent currentEvent)
      {
         // TODO Remote message support ?
         if (currentEvent != null)
         {
            lock (_currentEvents)
            {
               foreach (var ce in _currentEvents)
               {
                  // Do we already have this current event?
                  if (ce.CurrentEvent.Id == currentEvent.Id) return;
               }
               Logger.Debug("ADD CurrentEvent [{0}:{1}]", currentEvent.Id, currentEvent.Point.Id);
               var currentEventData = new CurrentEventData
               {
                  NewFlag = true,
                  CurrentEvent = currentEvent,
                  Priority = FindPriority(currentEvent.Point.Priority)
               };
               _currentEvents.Add(currentEventData);
               _currentEvents.Sort(_currentEventDataSorter);
               // Reset sounds if its not repeating or already on a timer
               if (_lastSoundEvent != null &&
                   (currentEventData.Priority == _lastSoundEvent.Priority) &&
                   (currentEventData.Priority.Repeat == "0"))
               {
                  _lastSoundEvent = null;
               }
            }
         }
         _currentEventsUpdatedTime = DateTime.Now;
      }

      private void UpdateAnalogData(CurrentEvent currentEvent)
      {
         foreach (var heading in _headings)
         {
            if (heading.Type == "ANALOG" && heading.Id == currentEvent.Point.Address)
            {
               string result = "";
               if (heading.Format == "temperature")
               {
                  result = currentEvent.Point.Message1 + "°";
               }
               else if (heading.Format == "humidity") {
                  result = currentEvent.Point.Message1 + "%";
               }
               else
               {
                  result = currentEvent.Point.Message1;
               }
               this.InvokeOnUIThread(() => {
                  heading.Label.Text = result;
               });
            }
         }
      }

      private PriorityData FindPriority(int priority)
      {
         return _priorities.Where(p => p.Level == priority).FirstOrDefault();
      }

      private void RemoveCurrentEvent(int id)
      {
         lock (_currentEvents)
         {
            foreach (var ce in _currentEvents)
            {
               if (ce.CurrentEvent.Id == id)
               {
                  Logger.Debug("REMOVE CurrenEvent [{0}:{1}]", ce.CurrentEvent.Id, ce.CurrentEvent.Point.Id);
                  _currentEvents.Remove(ce);
                  break;
               }
            }
         }
         _currentEventsUpdatedTime = DateTime.Now;
      }

      private void HandleCurrentEventResponse(hasoftware.Api.Message message)
      {
         if (message.IsError)
         {
            var r = (ErrorResponse)message;
            foreach (var error in r.ErrorMessages)
            {
               Logger.Error("CurrentEventResponse: {0}", error.Message);
            }
         }
         else
         {
            var r = (CurrentEventResponse)message;
            foreach (var currentEvent in r.CurrentEvents)
            {
               if (currentEvent.Point.DeviceTypeCode == "POINT")
               {
                  AddCurrentEvent(currentEvent);
               }
               else if (currentEvent.Point.DeviceTypeCode == "TEMP")
               {
                  UpdateAnalogData(currentEvent);
               }
            }
         }
      }

      private void HandleNotifyResponse(hasoftware.Api.Message message)
      {
         var r = (NotifyResponse)message;
         if (r.NotifyFunctionCode == FunctionCode.CurrentEvent)
         {
            switch (r.Action)
            {
               case CdefAction.Create:
                  var currentEventRequest = new CurrentEventRequest();
                  currentEventRequest.Action = CdefAction.List;
                  currentEventRequest.Ids.AddRange(r.Ids);
                  _eventQueue.Enqueue(new Event(EventType.SendMessage, currentEventRequest));
                  break;

               case CdefAction.Delete:
                  foreach (var id in r.Ids)
                  {
                     RemoveCurrentEvent(id);
                  }
                  break;
            }
         }
      }

      private void HandleLoginResponse(hasoftware.Api.Message message)
      {
         if (message.IsError)
         {
            _status = "Login Error";
            var r = (ErrorResponse)message;
            foreach (var error in r.ErrorMessages)
            {
               Logger.Error("LoginResponse: {0}", error.Message);
            }
         }
         else
         {
            _status = "Online";
            var notifyRequest = new NotifyRequest();
            notifyRequest.FunctionCodes.Add(FunctionCode.CurrentEvent);
            _eventQueue.Enqueue(new Event(EventType.SendMessage, notifyRequest));
            var currentEventRequest = new CurrentEventRequest();
            currentEventRequest.Action = CdefAction.List;
            _eventQueue.Enqueue(new Event(EventType.SendMessage, currentEventRequest));
         }
      }

      private void SendEvent(Event e)
      {
         if (e.Type != EventType.TimeCheck)
         {
            Logger.Debug("SendEvent [{0} @{1}]", e.Type, e.Time / 1000);
         }
         foreach (IMessageStream messageStream in _messageStreams.Values)
         {
            messageStream.HandleEvent(e);
         }
      }

      private void OnExit(object sender, EventArgs e)
      {
         Close();
      }

      private void OnFormClosing(object sender, FormClosingEventArgs e)
      {
         _eventQueue.Enqueue(new Event(EventType.Shutdown));
         foreach (IMessageStream messageStream in _messageStreams.Values)
         {
            messageStream.ShutDown();
         }
      }

      private void OnContextOpening(object sender, CancelEventArgs e)
      {
         Cursor.Show();
      }

      private void OnContextClosing(object sender, ToolStripDropDownClosingEventArgs e)
      {
         Cursor.Hide();
      }

      private void OnTimerTick(object sender, EventArgs e)
      {
         // Display
         if (_flashCounter++ > 3) _flashFlag = !_flashFlag;
         var now = DateTime.Now;
         SuspendLayout();
         UpdateHeadings(now);
         UpdateCurrentEvents(false);
         UpdateCurrentEventsColors();
         ResumeLayout();
         // Sounds
         var soundEvent = DetermineSoundEvent();
         _soundVolume = DetermineVolume(soundEvent);
         bool stoppedSound = false;
         if (ShouldStopSound(soundEvent, _lastSoundEvent))
         {
            _soundPlayer.Stop();
            _soundPlayer.Stream = null;
            _soundTimerFlag = false;
            _soundTimer = 0;
            stoppedSound = true;
         }
         if (soundEvent != null && stoppedSound)
         {
            _soundStream = GetSoundStream(soundEvent);
            if (_soundStream != null)
            {
               waveOutSetVolume(IntPtr.Zero, _soundVolume);
               _soundPlayer.Stream = _soundStream;
               if (soundEvent.Priority.Repeat == "*")
               {
                  // This sound is constantly looped forever
                  _soundTimerFlag = false;
                  _soundTimer = 0;
                  _soundPlayer.PlayLooping();
               }
               else
               {
                  //The sound is repeated only if the repeat time is not zero
                  if (soundEvent.Priority.Repeat != "0")
                  {
                     _soundTimerFlag = true;
                     _soundTimer = int.Parse(soundEvent.Priority.Repeat);
                  }
                  else
                  {
                     _soundTimerFlag = false;
                     _soundTimer = 0;
                  }
                  _soundPlayer.Play();
               }
            }
         }
         _lastSoundEvent = soundEvent;
         // Handle repeating sounds
         if (_soundStream != null)
         {
            if (_soundTimerFlag)
            {
               if (_soundTimer > 0)
               {
                  _soundTimer--;
               }
               if (_soundTimer == 0)
               {
                  _soundPlayer.Stop();
                  _soundPlayer.Play();
                  _soundTimer = int.Parse(soundEvent.Priority.Repeat);
               }
            }
         }
      }

      private bool ShouldStopSound(CurrentEventData soundEvent, CurrentEventData lastSoundEvent)
      {
         if (lastSoundEvent != null && soundEvent == null)
         {
            //Console.WriteLine("ShouldStopSound: no next sound - stop");
            return true;
         }
         if (lastSoundEvent == null && soundEvent != null)
         {
            //Console.WriteLine("ShouldStopSound: new sound - stop");
            return true;
         }
         if (lastSoundEvent != null && soundEvent.Priority != lastSoundEvent.Priority)
         {
            //Console.WriteLine("ShouldStopSound: new priority - stop");
            return true;
         }
         if (lastSoundEvent != null && lastSoundEvent.CurrentEvent.Point.Message2 != soundEvent.CurrentEvent.Point.Message2)
         {
            //Console.WriteLine("ShouldStopSound: new sound file - stop");
            return true;
         }
         //Console.WriteLine("ShouldStopSound: no change - continue");
         return false;
      }

      private Stream GetSoundStream(CurrentEventData currentEventData)
      {
         if (currentEventData == null) { return null; }
         // By default we choose the priority sound
         PriorityData priority = currentEventData.Priority;
         var filename = priority.Sound;
         // Then check if the point has a message2 that starts with 'sounds'
         var point = currentEventData.CurrentEvent.Point;
         if (point.Message2 != null && point.Message2.StartsWith("sounds")) {
            filename = point.Message2;
         }
         return string.IsNullOrEmpty(filename) ? null : File.OpenRead(filename);
      }

      private uint DetermineVolume(CurrentEventData soundEvent)
      {
         // TODO
         return 0xFFFF; // Full volume
      }

      private CurrentEventData DetermineSoundEvent()
      {
         CurrentEventData result = null;
         var row = 0;
         while (_currentEventsDisplayed[row] != null)
         {
            var currentEventData = _currentEventsDisplayed[row];
            if (currentEventData.AcknowledgedFlag) continue;
            result = currentEventData;
            break;
         }
         return result;
      }

      private void UpdateCurrentEventsColors()
      {
         var row = 0;
         while (_currentEventsDisplayed[row] != null)
         {
            var currentEventData = _currentEventsDisplayed[row];
            foreach (var column in _columns)
            {
               // TODO Handle selected
               var aa = _flashFlag;
               var bb = currentEventData.Priority.Flash;
               if (aa == true && bb == true)
               {
                  column.Labels[row].BackColor = currentEventData.Priority.BackColor2;
                  column.Labels[row].ForeColor = currentEventData.Priority.ForeColor;
               }
               else
               {
                  column.Labels[row].BackColor = currentEventData.Priority.BackColor1;
                  column.Labels[row].ForeColor = currentEventData.Priority.ForeColor;
               }
            }
            row++;
         }
      }

      private void UpdateCurrentEvents(bool forcedUpdate)
      {
         _newCurrentEventDisplayedFlag = false;
         var now = DateTime.Now.CurrentTimeMillis();
         if (forcedUpdate || _currentEventsUpdatedTime != _currentEventsDisplayedTime)
         {
            _currentEventsDisplayedTime = _currentEventsUpdatedTime;
            var rowsUsed = 0;
            var currentEventsUsed = 0;
            lock (_currentEvents)
            {
               while (rowsUsed < _rowCount)
               {
                  if (_currentEvents.Count == 0 || currentEventsUsed == _currentEvents.Count) break;
                  var currentEventData = _currentEvents[rowsUsed];
                  _currentEventsDisplayed[rowsUsed] = currentEventData;
                  if (currentEventData.NewFlag)
                  {
                     currentEventData.NewFlag = false;
                     _newCurrentEventDisplayedFlag = true;
                  }
                  foreach (var column in _columns)
                  {
                     // TODO multiline support
                     var message = GetMessages(now, currentEventData.CurrentEvent, column.Format);
                     column.Labels[rowsUsed].Text = message;
                     column.Labels[rowsUsed].Visible = true;
                     column.Labels[rowsUsed].ForeColor = currentEventData.Priority.ForeColor;
                     column.Labels[rowsUsed].BackColor = currentEventData.Priority.BackColor1;
                  }
                  currentEventsUsed++;
                  rowsUsed++;
               }
            }
            while (rowsUsed < _rowCount)
            {
               _currentEventsDisplayed[rowsUsed] = null;
               foreach (var column in _columns)
               {
                  column.Labels[rowsUsed].Visible = false;
               }
               rowsUsed++;
            }
         }
         else
         {
            // Just update the elapsed time columns
            foreach (var column in _columns)
            {
               if (column.Format.Contains(FormatElapsedTime))
               {
                  var row = 0;
                  while (_currentEventsDisplayed[row] != null)
                  {
                     var message = GetMessages(now, _currentEventsDisplayed[row].CurrentEvent, column.Format);
                     column.Labels[row].Text = message;
                     row++;
                  }
               }
            }
         }
      }

      private string GetMessages(long now, CurrentEvent currentEvent, string format)
      {
         var fullMessage = GetFormattedMessage(now, currentEvent, format);
         // TODO Multiline
         return fullMessage;
      }

      private string GetFormattedMessage(long now, CurrentEvent currentEvent, string format)
      {
         // Elapsed time
         var elapsedTime = "";
         if (format.Contains(FormatElapsedTime))
         {
            int seconds = (int)((now - currentEvent.CreatedOn) / 1000);
            var span = new TimeSpan(0, 0, seconds);
            if (span.Hours == 0)
            {
               elapsedTime = string.Format("{0:00}:{1:00}", span.Minutes, span.Seconds);
            }
            else if (span.Days == 0)
            {
               elapsedTime = string.Format("{0:00}:{1:00}:{2:00}", span.Hours, span.Minutes, span.Seconds);
            }
            else
            {
               elapsedTime = string.Format("{0}d {1}h", span.Days, span.Hours);
            }
         }
         // TODO Proper priority codes
         var result = format;
         if (result.Contains("$P"))
         {
            var priorityStr = "?";
            var priority = _priorities.Find(p => p.Level == currentEvent.Point.Priority);
            if (priority != null)
            {
               priorityStr = priority.Code;
            }
            result = result.Replace("$P", priorityStr);
         }
         result = result.Replace("$M1", currentEvent.Point.Message1);
         result = result.Replace("$E", elapsedTime);
         return result;
      }

      private void UpdateHeadings(DateTime now)
      {
         foreach (var heading in _headings)
         {
            if (heading.Type == "STATUS")
            {
               heading.Label.Text = _status;
            }
            else if (heading.Type == "TIME")
            {
               heading.Label.Text = now.ToString(heading.Format);
            }
            else if (heading.Type == "ANALOG")
            {
               // To nothing
            }
            else if (heading.Type == "FILE")
            {
               if (File.Exists(heading.Filename))
               {
                  if (heading.RTTimer > heading.Refresh)
                  {
                     var sr = File.OpenText(heading.Filename);
                     var line = sr.ReadLine();
                     sr.Close();
                     heading.Label.Text = line;
                     heading.RTTimer = 0;
                  }
                  if (heading.Refresh != 0)
                  {
                     heading.RTTimer++;
                  }
               }
            }
         }
      }

      [DllImport("winmm.dll")]
      public static extern int waveOutSetVolume(IntPtr hwo, uint dwVolume);
   }
}
