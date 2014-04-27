using System;
using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class Columns : Base
    {
        public const string CName = "columns";

        public List<Column> ColumnList { get; private set; }

        public Columns(XmlNode node)
        {
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType != XmlNodeType.Element) continue;
                switch (child.Name)
                {
                    case Column.CName:
                        ColumnList.Add(new Column(child));
                        break;
                }
            }
        }

        public Column GetColumn(string name)
        {
            foreach (var c in ColumnList)
            {
                if (c.Name.Equals(name))
                {
                    return c;
                }
            }
            throw new ApplicationException("Unknown column " + name);
        }
    }
}
