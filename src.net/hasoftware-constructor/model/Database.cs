using System.Xml;

namespace hasoftware.model
{
    public class Database : Base
    {
        public const string CName = "database";

        public Tables Tables { get; private set; }

        public Database(XmlNode node)
        {
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType != XmlNodeType.Element) continue;
                switch (child.Name)
                {
                    case Tables.CName:
                        Tables = new Tables(child);
                        break;
                }
            }
        }
    }
}
