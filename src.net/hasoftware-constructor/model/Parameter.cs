using System.Xml;

namespace hasoftware.model
{
    public class Parameter : Base
    {
        public const string CName = "parameter";

        private const string AName = "name";
        private const string AType = "type";
        private const string AList = "list";
        private const string ADefault = "default";

        public Parameter(XmlNode node)
        {
            SetValue(AName, node.Attributes[AName].Value);
            SetValue(AType, node.Attributes[AType].Value);
            SetValue(AList, node.Attributes[AList].Value);
            SetValue(ADefault, node.Attributes[ADefault].Value);
        }

        public string Name
        {
            get { return GetValue(AName); }
            set { SetValue(AName, value); }
        }

        public string Type
        {
            get { return GetValue(AType); }
            set { SetValue(AType, value); }
        }

        public string Default
        {
            get { return GetValue(ADefault); }
            set { SetValue(ADefault, value); }
        }

        public bool IsList
        {
            get { return bool.Parse(GetValue(AList)); }
        }
    }
}
