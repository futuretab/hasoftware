using System.Xml;

namespace hasoftware.model
{
    public class Keys : Base
    {
        public const string CName = "keys";

        public PrimaryKey PrimaryKey { get; private set; }

        public Keys(XmlNode node)
        {
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType != XmlNodeType.Element) continue;
                switch (child.Name)
                {
                    case PrimaryKey.CName:
                        PrimaryKey = new PrimaryKey(child);
                        break;
                }
            }
        }
    }
}
