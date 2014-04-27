using hasoftware.Api;
using hasoftware.Cdef;
using hasoftware.Classes;
using hasoftware.Configuration;
using hasoftware.Messages;
using hasoftware.Util;
using hasoftware_current_calls.Util;
using NLog;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Media;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace hasoftware_current_calls.Forms
{
    public partial class CurrentCallsForm : Form
    {
        private static Logger logger = LogManager.GetCurrentClassLogger();

        private const string XmlConfigurationFile = "current-calls.xml";
        private const int TimeCheckPeriod = 1000;
        private const string FormatElapsedTime = "$E";

        private XmlConfiguration _xmlConfiguration;
        private SoundPlayer _soundPlayer;
        private string _fontName;
        private float _fontSize;
        private Font _font;
        private List<HeadingConfiguration> _headings;
        private CdefClient _client;
        private ConcurrentQueue<Event> _eventQueue;
        private Event _timeCheckEvent;
        private string _status;
        private int _rowCount;
        private List<ColumnConfiguration> _columns;
        private List<CurrentEventData> _currentEvents;
        private CurrentEventDataSorter _currentEventDataSorter;
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
            LoadSettings();
        }

        private void LoadSettings()
        {
            logger.Debug("-- LOAD SETTINGS START --");
            SuspendLayout();
            _xmlConfiguration = new XmlConfiguration();
            if (!_xmlConfiguration.Load(XmlConfigurationFile))
            {
                MessageBox.Show("Can not load the configuration file: " + XmlConfigurationFile, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            Options.Load(_xmlConfiguration);
            using (_xmlConfiguration.XmlBuffer)
            {
                logger.Debug("-- LOAD COMMS SETTINGS --");
                {
                    var settings = _xmlConfiguration.GetSettings("configuration/comms/stream0");
                    if (settings != null)
                    {
                        var host = settings.GetString("host", "localhost");
                        var port = settings.GetInt("port", 6969);
                        _client = new CdefClient(host, port);
                        _client.SetEventQueue(_eventQueue);
                        _client.StartUp();
                        _status = "Connecting ...";
                        logger.Debug("SERVER [{0}:{1}]", host, port);
                    }
                    else
                    {
                        _status = "No coms stream defined!";
                    }
                }

                logger.Debug("-- LOAD FONT SETTINGS --");
                {
                    var settings = _xmlConfiguration.GetSettings("configuration/display/font");
                    _fontName = settings.GetString("name", "Tahoma");
                    _fontSize = settings.GetFloat("size", 28);
                    _font = new Font(_fontName, _fontSize);
                    logger.Debug("DEFAULT FONT [{0} {1}]", _fontName, _fontSize);
                }

                logger.Debug("-- LOAD DISPLAY SETTINGS --");
                {
                    var screenCount = 0;
                    foreach (var screen in Screen.AllScreens)
                    {
                        logger.Debug(string.Format("[{0}] [{1}x{2}]@{3} {4}", screenCount++, screen.Bounds.Width, screen.Bounds.Height, screen.BitsPerPixel, screen.Primary));
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
                        logger.Debug("FULLSCREEN MODE ON SCREEN {0}", tempInt);
                    }
                    else
                    {
                        var width = settings.GetInt("width", 1024);
                        var height = settings.GetInt("height", 768);
                        ClientSize = new Size(width, height);
                        FormBorderStyle = FormBorderStyle.FixedSingle;
                        MaximizeBox = false;
                        logger.Debug("WINDOW MODE");
                    }
                }

                logger.Debug("-- LOAD BACKGROUND IMAGE --");
                {
                    var settings = _xmlConfiguration.GetSettings("configuration/display/background");
                    var filename = settings.GetString("filename", null);
                    if (!string.IsNullOrEmpty(filename) && File.Exists(filename))
                    {
                        BackgroundImage = Image.FromFile(filename);
                        logger.Debug("USING IMAGE [{0}]", filename);
                    }
                    else
                    {
                        var color = settings.GetString("back-color", "White");
                        BackColor = Color.FromName(color);
                        logger.Debug("USING COLOR [{0}]", color);
                    }
                }

                logger.Debug("-- LOAD LABEL SETTINGS --");
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

                logger.Debug("-- LOAD PRIORITIES --");
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
                        var parts = configuration.Split(new [] {'|'}, StringSplitOptions.RemoveEmptyEntries);
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

                logger.Debug("-- LOAD ROWS AND COLUMNS --");
                {
                    _columns = new List<ColumnConfiguration>();
                    var settings = _xmlConfiguration.GetSettings("configuration/rows");
                    _rowCount = settings.GetInt("count", 10);
                    _currentEventsDisplayed = new CurrentEventData[_rowCount];
                    var height = settings.GetInt("height", 35);
                    var offset = settings.GetInt("offset", 10);
                    var y = settings.GetInt("y", 40);
                    var columnIndex = 0;
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

                        var fontName = settings.GetString("font-name", _fontName);
                        var fontSize = settings.GetFloat("font-size", _fontSize);
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
                        for (var i=0; i<column.Labels.Length; i++) {
                            var label = new Label {
                                //BackColor = Color.Transparent,
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
            Task.Run(() =>
            {
                PassEvents();
            });
            _timer.Start();
            logger.Debug("-- LOAD SETTINGS STOP --");
        }

        private void PassEvents() {
            long lastTimeCheck = 0;

            while (true) {
                Event e = null;
                var suceess = _eventQueue.TryDequeue(out e);
                if (e != null) {
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
                                case FunctionCode.Login: HandleLoginResponse(e.Message); break;
                                case FunctionCode.Notify: HandleNotifyResponse(e.Message); break;
                                case FunctionCode.CurrentEvent: HandleCurrentEventResponse(e.Message); break;
                            }
                            break;
                    }
                }
                long now = DateTime.Now.CurrentTimeMillis();
                if ((now - lastTimeCheck) > TimeCheckPeriod) {
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
                    logger.Debug("ADD CurrentEvent [{0}:{1}]", currentEvent.Id, currentEvent.Point.Id);
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
                        (currentEventData.Priority.Repeat == "0")) {
                        _lastSoundEvent = null;
                    }
                }
            }
            _currentEventsUpdatedTime = DateTime.Now;
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
                        logger.Debug("REMOVE CurrenEvent [{0}:{1}]", ce.CurrentEvent.Id, ce.CurrentEvent.Point.Id);
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
                    logger.Error("CurrentEventResponse: {0}", error.Message);
                }
            }
            else
            {
                var r = (CurrentEventResponse)message;
                foreach (var currentEvent in r.CurrentEvents)
                {
                    AddCurrentEvent(currentEvent);
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
                    logger.Error("LoginResponse: {0}", error.Message);
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

        private void SendEvent(Event e) {
            if (e.Type != EventType.TimeCheck) {
                logger.Debug("SendEvent [{} @{}]", e.Type, e.Time / 1000);
            }
            if (_client != null)
            {
                _client.HandleEvent(e);
            }
        }

        private void OnExit(object sender, EventArgs e)
        {
            _eventQueue.Enqueue(new Event(EventType.Shutdown));
            _client.ShutDown();
            Close();
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
            if (_lastSoundEvent != null &&
                (soundEvent == null || soundEvent.Priority != _lastSoundEvent.Priority))
            {
                _soundPlayer.Stop();
                _soundPlayer.Stream = null;
                _soundTimerFlag = false;
                _soundTimer = 0;
            }
            if (soundEvent != null &&
                (_lastSoundEvent == null || soundEvent.Priority != _lastSoundEvent.Priority))
            {
                _soundStream = GetSoundStream(soundEvent.Priority);
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

        private Stream GetSoundStream(PriorityData priority)
        {
            var filename = priority.Sound;
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
                    if (_flashFlag && currentEventData.Priority.Flash)
                    {
                        column.Labels[row].BackColor = currentEventData.Priority.BackColor2;
                    }
                    else
                    {
                        column.Labels[row].BackColor = currentEventData.Priority.BackColor1;
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
                        column.Labels[rowsUsed].Text = "";
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
            result = result.Replace("$P", "" + currentEvent.Point.Priority);
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
