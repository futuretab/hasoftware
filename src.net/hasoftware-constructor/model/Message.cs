using System.Xml;

namespace hasoftware.model
{
    public class Message : Base
    {
        public const string CName = "message";

        private const string AName = "name";
        private const string ACode = "code";

        public Description Description { get; private set; }
        public Request Request { get; private set; }
        public Response Response { get; private set; }

        public Message(XmlNode node)
        {
            SetValue(AName, node.Attributes[AName].Value);
            SetValue(ACode, node.Attributes[ACode].Value);
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType == XmlNodeType.Element)
                {
                    switch (child.Name)
                    {
                        case Description.CName:
                            Description = new Description(child);
                            break;

                        case Request.CName:
                            Request = new Request(child);
                            break;

                        case Response.CName:
                            Response = new Response(child);
                            break;
                    }
                }
            }
        }

        public string Name
        {
            get { return GetValue(AName); }
            set { SetValue(AName, value); }
        }

        public string Code
        {
            get { return GetValue(ACode); }
            set { SetValue(ACode, value); }
        }
    }
}
