using System.Xml;

namespace hasoftware.model
{
    public class Specification : Base
    {
        public const string CName = "specification";

        private const string ACompany = "company";
        private const string AProject = "project";
        private const string AJavaBaseDirectory = "java-base-directory";
        private const string ANetBaseDirectory = "net-base-directory";
        private const string ANamespace = "namespace";

        public Classes Classes { get; private set; }
        public Messages Messages { get; private set; }
        public Database Database { get; private set; }

        public Specification(XmlNode node)
        {
            SetValue(ACompany, node.Attributes[ACompany].Value);
            SetValue(AProject, node.Attributes[AProject].Value);
            SetValue(AJavaBaseDirectory, node.Attributes[AJavaBaseDirectory].Value);
            SetValue(ANetBaseDirectory, node.Attributes[ANetBaseDirectory].Value);
            SetValue(ANamespace, node.Attributes[ANamespace].Value);
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType != XmlNodeType.Element) continue;
                switch (child.Name)
                {
                    case Classes.CName:
                        Classes = new Classes(child);
                        break;

                    case Messages.CName:
                        Messages = new Messages(child);
                        break;

                    case Database.CName:
                        Database = new Database(child);
                        break;
                }
            }
        }

        public string Company
        {
            get { return AttributeValues[ACompany]; }
            set { AttributeValues[ACompany] = value; }
        }

        public string Project
        {
            get { return AttributeValues[AProject]; }
            set { AttributeValues[AProject] = value; }
        }

        public string JavaBaseDirectory
        {
            get { return AttributeValues[AJavaBaseDirectory]; }
            set { AttributeValues[AJavaBaseDirectory] = value; }
        }

        public string NetBaseDirectory
        {
            get { return AttributeValues[ANetBaseDirectory]; }
            set { AttributeValues[ANetBaseDirectory] = value; }
        }

        public string Namespace
        {
            get { return AttributeValues[ANamespace]; }
            set { AttributeValues[ANamespace] = value; }
        }
    }
}
