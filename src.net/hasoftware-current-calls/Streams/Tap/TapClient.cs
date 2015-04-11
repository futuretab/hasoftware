using hasoftware.Util;
using hasoftware_current_calls.Util;
using NLog;
using System;
using System.IO;
using System.Net.Sockets;
using System.Text;

namespace hasoftware_current_calls.Streams.Tap
{
    public class TapClient
    {
        private static Logger logger = LogManager.GetCurrentClassLogger();

        private TimeSpan DefaultTimeout = TimeSpan.FromSeconds(5);
        private const int MaxLineLength = 1024;
        private const int MaxBufferLength = 1024;

        private TcpClient _tcpClient;

        public TimeSpan Timeout { get; set; }

        public TapClient(TcpClient tcpClient)
        {
            logger.Debug("Creating TapClient");
            _tcpClient = tcpClient;
            Timeout = DefaultTimeout;
        }

        public bool Connected
        {
            get
            {
                if (_tcpClient.Client.Poll(0, SelectMode.SelectRead))
                {
                    var buffer = new byte[1];
                    if (_tcpClient.Client.Receive(buffer, SocketFlags.Peek) == 0)
                    {
                        return false;
                    }
                }
                return true;
            }
        }

        public bool WaitFor(string response)
        {
            return WaitFor(response, Timeout);
        }

        public bool WaitFor(string response, TimeSpan timeout)
        {
            var result = false;
            var data = new byte[MaxLineLength + 1];
            var startTime = DateTime.Now;
            while (!result)
            {
                var message = InternalRead(50);
                if (message != null)
                {
                    logger.Debug("WaitFor [{0}] got [{1}]", response, message);
                    if (message == response)
                    {
                        return true;
                    }
                }
                else
                {
                    var elapsedTime = DateTime.Now.Subtract(startTime);
                    if (elapsedTime > timeout)
                    {
                        logger.Debug("WaitFor timeout");
                        return false;
                    }
                }
            }
            return result;
        }

        private byte[] _readAheadBuffer = new byte[MaxBufferLength + 1];
        private int _readAheadOffset = 0;

        private string InternalRead(int timeout)
        {
            string response = null;

            var len = -1;
            var stream = _tcpClient.GetStream();
            stream.ReadTimeout = timeout;
            try
            {
                len = stream.Read(_readAheadBuffer, _readAheadOffset, _readAheadBuffer.Length - _readAheadOffset);
                _readAheadOffset = _readAheadOffset + len;
                if (_readAheadOffset > 0)
                {
                    _readAheadBuffer[_readAheadOffset] = 0;
                    len = Utils.ByteSearch(_readAheadBuffer, Serial.CR, _readAheadOffset);
                    if (len != -1)
                    {
                        response = Encoding.UTF8.GetString(_readAheadBuffer, 0, len);
                        TrimBuffer(len + 1);
                    }
                    while (_readAheadOffset >= 1 && _readAheadBuffer[0] == Serial.LF)
                    {
                        TrimBuffer(1);
                    }
                }
            }
            catch (IOException ex)
            {
                //logger.Debug("Exception {0}", ex.Message);
            }
            return response;
        }

        private void TrimBuffer(int len)
        {
            Array.Copy(_readAheadBuffer, _readAheadOffset + len, _readAheadBuffer, 0, _readAheadOffset - len);
            _readAheadOffset = _readAheadOffset - len;
            _readAheadBuffer[_readAheadOffset] = 0;
        }

        public bool Send(string command, bool appendCr = false)
        {
            if (appendCr)
            {
                command = command + (char)Serial.CR;
            }
            var commandBytes = Encoding.UTF8.GetBytes(command);
            _tcpClient.GetStream().Write(commandBytes, 0, commandBytes.Length);
            return true;
        }

        public bool Receive(out string message)
        {
            message = InternalRead(100);
            return message != null;
        }

        internal void Close()
        {
            _tcpClient.Close();
        }
    }
}
