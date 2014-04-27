using System;
using System.Collections.Generic;

namespace hasoftware.model
{
    public class Base
    {
        protected Dictionary<string, string> AttributeValues = new Dictionary<string, string>();

        public string GetValue(string key)
        {
            var value = AttributeValues[key];
            if (value == null)
            {
                throw new ApplicationException("Unknown attribute " + key);
            }
            return value;
        }

        public void SetValue(string key, string value)
        {
            AttributeValues[key] = value;
        }
    }
}
