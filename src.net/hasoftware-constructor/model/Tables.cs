using System;
using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class Tables : Base
    {
        public const string CName = "tables";

        public List<Table> TableList { get; private set; }

        public Tables(XmlNode node)
        {
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType != XmlNodeType.Element) continue;
                switch (child.Name)
                {
                    case Table.CName:
                        TableList.Add(new Table(child));
                        break;
                }
            }
        }

        public Table GetTable(string name)
        {
            foreach (var t in TableList)
            {
                if (t.Name.Equals(name))
                {
                    return t;
                }
            }
            throw new ApplicationException("Unknown table " + name);
        }
    }
}
