using System.Xml;

namespace hasoftware.model
{
    public class ColumnReference : Base
    {
        public const string CName = "column-reference";

        private const string AName = "name";

        public ColumnReference(XmlNode node)
        {
            SetValue(AName, node.Attributes[AName].Value);
        }

        public string Name
        {
            get { return GetValue(AName); }
            set { SetValue(AName, value); }
        }
    }
}
