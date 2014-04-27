using System.Xml;

namespace hasoftware.model
{
    public class Description : Base
    {
        public const string CName = "description";

        public Description(XmlNode node)
        {
            SetValue("value", node.InnerText);
        }

        public string Value
        {
            get { return AttributeValues["value"]; }
            set { AttributeValues["value"] = value; }
        }
    }
}
