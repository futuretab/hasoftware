using System;
using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class Classes : Base
    {
        public const string CName = "classes";

        public List<Class> ClassList { get; private set; }

        public Classes(XmlNode node)
        {
            ClassList = new List<Class>();
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType == XmlNodeType.Element)
                {
                    switch (child.Name)
                    {
                        case Class.CName:
                            ClassList.Add(new Class(child));
                            break;
                    }
                }
            }
        }

        public Class GetClass(string name)
        {
            foreach (var c in ClassList)
            {
                if (c.Name.Equals(name))
                {
                    return c;
                }
            }
            throw new ApplicationException("Unknown class " + name);
        }

        public bool IsClass(string name)
        {
            foreach (var c in ClassList)
            {
                if (c.Name.Equals(name))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
