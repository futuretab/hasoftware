using System.Xml;

namespace hasoftware.model
{
    public class Table : Base
    {
        public const string CName = "table";

        private const string AName = "name";

        public Keys Keys { get; private set; }
        public Columns Columns { get; private set; }

        public Table(XmlNode node)
        {
            SetValue(AName, node.Attributes[AName].Value);
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType != XmlNodeType.Element) continue;
                switch (child.Name)
                {
                    case Columns.CName:
                        Columns = new Columns(child);
                        break;

                    case Keys.CName:
                        Keys = new Keys(child);
                        break;
                }
            }
        }

        public string Name
        {
            get { return GetValue(AName); }
            set { SetValue(AName, value); }
        }
    }
}
