using System.Xml;

namespace hasoftware.model
{
    public class Attribute : Base
    {
        public const string CName = "attribute";

        private const string AName = "name";
        private const string AType = "type";
        private const string ALength = "length";
        private const string ANullable = "nullable";
        private const string AList = "list";

        public Attribute(XmlNode node)
        {
            SetValue(AName, node.Attributes[AName].Value);
            SetValue(AType, node.Attributes[AType].Value);
            SetValue(ALength, node.Attributes[ALength].Value);
            SetValue(ANullable, node.Attributes[ANullable].Value);
            SetValue(AList, node.Attributes[AList].Value);
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

        public string Length
        {
            get { return GetValue(ALength); }
            set { SetValue(ALength, value); }
        }

        public string Nullable
        {
            get { return GetValue(ANullable); }
            set { SetValue(ANullable, value); }
        }

        public bool IsList
        {
            get { return bool.Parse(GetValue(AList)); }
        }
    }
}
