using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class PrimaryKey : Base
    {
        public const string CName = "primary-key";

        public List<ColumnReference> ColumnReferenceList { get; private set; }

        public PrimaryKey(XmlNode node)
        {
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType != XmlNodeType.Element) continue;
                switch (child.Name)
                {
                    case ColumnReference.CName:
                        ColumnReferenceList.Add(new ColumnReference(child));
                        break;
                }
            }
        }

        
    }
}
