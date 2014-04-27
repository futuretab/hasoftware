using System.Xml;

namespace hasoftware.model
{
    public class Column : Base
    {
        public const string CName = "column";

        private const string AName = "name";
        private const string AType = "type";
        private const string ALength = "length";
        private const string ADefaultValue = "default-value";
        private const string ADefaultType = "default-type";
        private const string ANullable = "nullable";

        public Column(XmlNode node)
        {
            SetValue(AName, node.Attributes[AName].Value);
            SetValue(AType, node.Attributes[AType].Value);
            SetValue(ALength, node.Attributes[ALength].Value);
            SetValue(ADefaultValue, node.Attributes[ADefaultValue].Value);
            SetValue(ADefaultType, node.Attributes[ADefaultType].Value);
            SetValue(ANullable, node.Attributes[ANullable].Value);
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

        public string DefaultValue
        {
            get { return GetValue(ADefaultValue); }
            set { SetValue(ADefaultValue, value); }
        }

        public string DefaultType
        {
            get { return GetValue(ADefaultType); }
            set { SetValue(ADefaultType, value); }
        }

        public string Nullable
        {
            get { return GetValue(ANullable); }
            set { SetValue(ANullable, value); }
        }
    }
}
