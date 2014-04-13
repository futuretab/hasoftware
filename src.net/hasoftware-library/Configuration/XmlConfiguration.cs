using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;

namespace hasoftware.Configuration
{
    public class XmlConfiguration
    {
        public XmlBuffer XmlBuffer { get; set; }
        public string XmlConfigurationFile { get; private set; }
        public bool ReadOnly { get; private set; }

        public XmlConfiguration()
        {
            ReadOnly = false;
        }

        public XmlConfiguration(string xmlConfigurationFile)
            : this()
        {
            Load(xmlConfigurationFile);
        }

        public bool Load(string xmlConfigurationFile)
        {
            if (File.Exists(xmlConfigurationFile))
            {
                XmlConfigurationFile = xmlConfigurationFile;
                return true;
            }
            return false;
        }

        public void VerifyFilename()
        {
            if (string.IsNullOrEmpty(XmlConfigurationFile))
            {
                throw new ArgumentException("Filename not set");
            }
        }

        public void VerifyPath(ref string path)
        {
            if (string.IsNullOrEmpty(path))
            {
                throw new ArgumentException("Path not set");
            }
        }

        public XmlSettings GetSettings(string path)
        {
            XmlSettings result = null;
            VerifyPath(ref path);
            try
            {
                XmlDocument doc = XmlDocument;
                XmlElement root = doc.DocumentElement;
                XmlNode node = root.SelectSingleNode(path);
                if (node != null)
                {
                    result = new XmlSettings();
                    foreach (XmlAttribute att in node.Attributes)
                    {
                        result.Add(att.Name, att.Value);
                    }
                }
            }
            catch
            {
            }
            return result;
        }

        protected XmlDocument XmlDocument
        {
            get
            {
                if (XmlBuffer != null)
                {
                    return XmlBuffer.XmlDocument;
                }
                VerifyFilename();
                if (File.Exists(XmlConfigurationFile) == false) { return null; }
                XmlDocument doc = new XmlDocument();
                doc.Load(XmlConfigurationFile);
                return doc;
            }
        }
    }
}
