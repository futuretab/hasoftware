using System;
using System.Drawing;

namespace hasoftware_current_calls.Util
{
    public class Utils
    {
        public static int ByteSearch(byte[] serachIn, byte searchByte, int maxLength)
        {
            for (var i = 0; i < maxLength; i++)
            {
                if (serachIn[i] == searchByte) return i;
            }
            return -1;
        }

        public static int ByteSearch(byte[] searchIn, byte[] searchBytes, int offset = 0)
        {
            var found = -1;
            var matched = false;
            if (searchIn.Length > 0 && searchBytes.Length > 0 &&
                offset <= (searchIn.Length - searchBytes.Length) &&
                searchIn.Length >= searchBytes.Length)
            {
                for (var i = offset; i <= searchIn.Length - searchBytes.Length; i++)
                {
                    if (searchIn[i] == searchBytes[0])
                    {
                        if (searchIn.Length > 1)
                        {
                            matched = true;
                            for (int y = 1; y <= searchBytes.Length - 1; y++)
                            {
                                if (searchIn[i + y] != searchBytes[y])
                                {
                                    matched = false;
                                    break;
                                }
                            }
                            if (matched)
                            {
                                found = i;
                                break;
                            }
                        }
                        else
                        {
                            found = i;
                            break;
                        }
                    }
                }
            }
            return found;
        }

        public static ContentAlignment GetTextAlignment(string data)
        {
            data = data.ToUpper();
            switch (data)
            {
                case "L":
                case "LEFT":
                    return ContentAlignment.MiddleLeft;

                case "R":
                case "RIGHT":
                    return ContentAlignment.MiddleRight;

                default:
                    return ContentAlignment.MiddleCenter;
            }
        }
    }
}
