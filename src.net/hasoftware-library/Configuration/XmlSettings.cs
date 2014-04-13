using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Configuration
{
    public class XmlSettings
    {
        private Dictionary<string, string> mSettings = new Dictionary<string, string>();

        public void Add(string name, string data)
        {
            mSettings[name] = data;
        }

        public int Count
        {
            get { return mSettings.Count; }
        }

        public string GetString(string name)
        {
            if (!mSettings.ContainsKey(name))
            {
                return null;
            }
            return mSettings[name] as string;
        }

        public int GetInt(string name, int defaultValue)
        {
            string result = GetString(name);
            if (result == null)
            {
                return defaultValue;
            }
            return int.Parse(result);
        }

        public float GetFloat(string name, float defaultValue)
        {
            string result = GetString(name);
            if (result == null)
            {
                return defaultValue;
            }
            return float.Parse(result);
        }

        public string GetString(string name, string defaultValue)
        {
            string result = GetString(name);
            return (result == null)? defaultValue : result;
        }
    }
}
