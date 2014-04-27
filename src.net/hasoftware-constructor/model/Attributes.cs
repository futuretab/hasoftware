using System;
using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class Attributes : Base
    {
        public const string CName = "attributes";

        public List<Attribute> AttributeList { get; private set; }

        public Attributes(XmlNode node)
        {
            AttributeList = new List<Attribute>();
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType == XmlNodeType.Element)
                {
                    switch (child.Name)
                    {
                        case Attribute.CName:
                            AttributeList.Add(new Attribute(child));
                            break;
                    }
                }
            }
        }

        public Attribute GetAttribute(string name)
        {
            foreach (var a in AttributeList)
            {
                if (a.Name.Equals(name))
                {
                    return a;
                }
            }
            throw new ApplicationException("Unknown attribute " + name);
        }
    }
}
