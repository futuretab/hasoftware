using System;
using System.ComponentModel;
namespace hasoftware.Util
{
    public class RandomUtils
    {
        private static Random _random;

        public static string RandomString(int length)
        {
            if (_random == null)
            {
                _random = new Random();
            }
            var result = "";
            for (var i = 0; i < length; i++)
            {
                result += (char)('A' + _random.Next(26));
            }
            return result;
        }

        public static void Set<T>(object owner, string name, ref T oldValue, T newValue, PropertyChangedEventHandler handler)
        {
            if (!Equals(oldValue, newValue))
            {
                oldValue = newValue;
                if (handler != null)
                {
                    handler(owner, new PropertyChangedEventArgs(name));
                }
            }
        }
    }
}
