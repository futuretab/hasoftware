using System.Xml;

namespace hasoftware.model
{
    public class Class : Base
    {
        public const string CName = "class";

        private const string AName = "name";

        public Class(XmlNode node)
        {
            SetValue(AName, node.Attributes[AName].Value);
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType == XmlNodeType.Element)
                {
                    switch (child.Name)
                    {
                        case Attributes.CName:
                            Attributes = new Attributes(child);
                            break;
                    }
                }
            }
        }

        public Attributes Attributes { get; private set; }

        public string Name
        {
            get { return GetValue(AName); }
            set { SetValue(AName, value); }
        }
    }
}
