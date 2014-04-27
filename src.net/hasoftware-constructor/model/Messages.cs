using System;
using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class Messages : Base
    {
        public const string CName = "messages";

        public List<Message> MessageList { get; private set; }

        public Messages(XmlNode node)
        {
            MessageList = new List<Message>();
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType == XmlNodeType.Element)
                {
                    switch (child.Name)
                    {
                        case Message.CName:
                            MessageList.Add(new Message(child));
                            break;
                    }
                }
            }
        }

        public Message GetMessage(string name)
        {
            foreach (var m in MessageList)
            {
                if (m.Name.Equals(name))
                {
                    return m;
                }
            }
            throw new ApplicationException("Unknown message " + name);
        }
    }
}
