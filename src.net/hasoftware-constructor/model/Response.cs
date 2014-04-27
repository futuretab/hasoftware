using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class Response : Base
    {
        public const string CName = "response";

        public List<Parameter> ParameterList { get; private set; }

        public Response(XmlNode node)
        {
            ParameterList = new List<Parameter>();
            foreach (XmlNode child in node.ChildNodes)
            {
                if (child.NodeType == XmlNodeType.Element)
                {
                    switch (child.Name)
                    {
                        case Parameter.CName:
                            ParameterList.Add(new Parameter(child));
                            break;
                    }
                }
            }
        }
    }
}
