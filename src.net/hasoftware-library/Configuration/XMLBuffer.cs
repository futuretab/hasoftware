using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;

namespace hasoftware.Configuration
{
    public class XmlBuffer : IDisposable
    {
        private XmlConfiguration _xmlConfiguration;
        private XmlDocument _xmlDocument;
        private FileStream _fileStream;
        private bool _needsFlushing;

        public XmlBuffer(XmlConfiguration xmlConfiguration, bool lockFile)
        {
            _xmlConfiguration = xmlConfiguration;
            if (lockFile)
            {
                _xmlConfiguration.VerifyFilename();
                if (File.Exists(_xmlConfiguration.XmlConfigurationFile) == true)
                {
                    _fileStream = new FileStream(_xmlConfiguration.XmlConfigurationFile, FileMode.Open, _xmlConfiguration.ReadOnly ? FileAccess.Read : FileAccess.ReadWrite, FileShare.Read);
                }
            }
        }

        public void Dispose()
        {
            Close();
        }

        internal XmlDocument XmlDocument
        {
            get
            {
                if (_xmlDocument == null)
                {
                    _xmlDocument = new XmlDocument();
                    if (_fileStream != null)
                    {
                        _fileStream.Position = 0;
                        _xmlDocument.Load(_fileStream);
                    }
                    else
                    {
                        _xmlConfiguration.VerifyFilename();
                        if (File.Exists(_xmlConfiguration.XmlConfigurationFile) == true)
                        {
                            _xmlDocument.Load(_xmlConfiguration.XmlConfigurationFile);
                        }
                    }
                }
                return _xmlDocument;
            }
        }

        public void Load(XmlTextWriter writer)
        {
            writer.Flush();
            writer.BaseStream.Position = 0;
            _xmlDocument.Load(writer.BaseStream);
            _needsFlushing = true;
        }

        public bool IsEmpty
        {
            get
            {
                return (_xmlDocument == null || _xmlDocument.InnerXml == String.Empty);
            }
        }

        public bool NeedsFlushing
        {
            get
            {
                return _needsFlushing;
            }
        }

        public bool Locked
        {
            get
            {
                return _fileStream != null;
            }
        }

        public void Flush()
        {
            if (_xmlConfiguration == null)
            {
                throw new InvalidOperationException("Cannot flush an XmlBuffer object that has been closed");
            }
            if (_xmlDocument == null) { return; }
            if (_fileStream == null)
            {
                _xmlDocument.Save(_xmlConfiguration.XmlConfigurationFile);
            }
            else
            {
                _fileStream.SetLength(0);
                _xmlDocument.Save(_fileStream);
            }
            _needsFlushing = false;
        }

        public void Reset()
        {
            if (_xmlConfiguration == null)
            {
                throw new InvalidOperationException("Cannot reset an XmlBuffer object that has been closed");
            }
            _xmlDocument = null;
            _needsFlushing = false;
        }

        public void Close()
        {
            if (_xmlConfiguration == null)
            {
                return;
            }
            if (_needsFlushing)
            {
                Flush();
            }
            _xmlDocument = null;
            if (_fileStream != null)
            {
                _fileStream.Close();
                _fileStream = null;
            }
            if (_xmlConfiguration != null)
            {
                _xmlConfiguration.XmlBuffer = null;
            }
            _xmlConfiguration = null;
        }
    }
}
