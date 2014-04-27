using System.Collections.Generic;
using System.Xml;

namespace hasoftware.model
{
    public class Request : Base
    {
        public const string CName = "request";

        public List<Parameter> ParameterList { get; private set; }

        public Request(XmlNode node)
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
