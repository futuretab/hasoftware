using System;
using hasoftware.model;

namespace hasoftware.handlers
{
    public class NetCodeHandler : IHandler
    {
        protected Specification Specification;

        public void Handle(Specification specification)
        {
            Specification = specification;
            HandleClasses();
            HandleMessages();
            HandleConstants();
        }

        private void HandleClasses()
        {
            foreach (var c in Specification.Classes.ClassList)
            {
                HandleClass(c);
            }
        }

        private void HandleMessages()
        {
            foreach (var m in Specification.Messages.MessageList)
            {
                HandleMessage(m);
            }
        }

        private void HandleMessage(Message data)
        {
            if (data.Response != null)
            {
                var f = HandlerSupport.OpenNetFilename("Messages", data.Name + "Response", ".cs");
                f.WriteLine("using System;");
                f.WriteLine("using System.Collections.Generic;");
                f.WriteLine("using {0}.Api;", Specification.Namespace);
                f.WriteLine("using {0}.Classes;", Specification.Namespace);
                f.WriteLine("using {0}.Cdef;", Specification.Namespace);
                f.WriteLine("");
                f.WriteLine("namespace {0}.Messages {{", Specification.Namespace);
                f.WriteLine("   public class {0}Response : Message {{", data.Name);

                // Attributes
                foreach (var p in data.Response.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("      public List<{0}> {1} {{ get; set; }}", GetType(p.Type),
                                    GetAccessorName(p.Name));
                    }
                    else
                    {
                        f.WriteLine("      public {0} {1} {{ get; set; }}", GetType(p.Type), GetAccessorName(p.Name));
                    }
                }
                if (data.Response.ParameterList.Count != 0) { f.WriteLine(""); }

                // Default constructor
                f.WriteLine("      public {0}Response(int transactionNumber) : base(Api.FunctionCode.{0}, transactionNumber, CdefSystemFlags.Response) {{", data.Name);
                foreach (var p in data.Response.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("         {0} = new List<{1}>();", GetAccessorName(p.Name), GetType(p.Type));
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                // CdefMessage Constructor
                f.WriteLine("      public {0}Response(CdefMessage cdefMessage) : base(cdefMessage) {{", data.Name);
                foreach (var p in data.Response.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("         {");
                        f.WriteLine("            {0} = new List<{1}>();", GetAccessorName(p.Name), GetType(p.Type));
                        f.WriteLine("            int c = cdefMessage.GetInt();");
                        f.WriteLine("            for (int i=0; i<c; i++) {");
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("               {0}.Add(new {1}(cdefMessage));", GetAccessorName(p.Name), p.Type);
                        }
                        else
                        {
                            f.WriteLine("               {0}.Add({1});", GetAccessorName(p.Name),
                                        GetCdefGet(GetType(p.Type)));
                        }
                        f.WriteLine("            }");
                        f.WriteLine("         }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("         {0} = new {1}(cdefMessage);", GetAccessorName(p.Name), p.Type);
                        }
                        else
                        {
                            f.WriteLine("         {0} = {1};", GetAccessorName(p.Name), GetCdefGet(GetType(p.Type)));
                        }
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                // Encode
                f.WriteLine("      public override void Encode(CdefMessage cdefMessage) {");
                f.WriteLine("         base.Encode(cdefMessage);");
                foreach (var p in data.Response.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("         cdefMessage.PutInt({0}.Count);", GetAccessorName(p.Name));
                        f.WriteLine("         foreach (var obj in {0}) {{", GetAccessorName(p.Name));
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("            obj.Encode(cdefMessage);");
                        }
                        else
                        {
                            f.WriteLine("            {0};", GetCdefPut(GetType(p.Type), "obj"));
                        }
                        f.WriteLine("         }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("         {0}.Encode(cdefMessage);", GetAccessorName(p.Name));
                        }
                        else
                        {
                            f.WriteLine("         {0};", GetCdefPut(GetType(p.Type), GetAccessorName(p.Name)));
                        }
                    }
                }
                f.WriteLine("      }");

                f.WriteLine("   }");
                f.WriteLine("}");
                f.Close();
            }

            if (data.Request != null)
            {
                var f = HandlerSupport.OpenNetFilename("Messages", data.Name + "Request", ".cs");
                f.WriteLine("using System;");
                f.WriteLine("using System.Collections.Generic;");
                f.WriteLine("using {0}.Api;", Specification.Namespace);
                f.WriteLine("using {0}.Classes;", Specification.Namespace);
                f.WriteLine("using {0}.Cdef;", Specification.Namespace);
                f.WriteLine("");
                f.WriteLine("namespace {0}.Messages {{", Specification.Namespace);
                f.WriteLine("   public class {0}Request : Message {{", data.Name);

                // Attributes
                foreach (var p in data.Request.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("      public List<{0}> {1} {{ get; set; }}", GetType(p.Type),
                                    GetAccessorName(p.Name));
                    }
                    else
                    {
                        f.WriteLine("      public {0} {1} {{ get; set; }}", GetType(p.Type), GetAccessorName(p.Name));
                    }
                }
                if (data.Request.ParameterList.Count != 0) { f.WriteLine(""); }

                // Default constructor
                f.WriteLine("      public {0}Request() {{", data.Name);
                f.WriteLine("         FunctionCode = Api.FunctionCode.{0};", data.Name);
                foreach (var p in data.Request.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("         {0} = new List<{1}>();", GetAccessorName(p.Name), GetType(p.Type));
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                // CdefMessage Constructor
                f.WriteLine("      public {0}Request(CdefMessage cdefMessage) : base(cdefMessage) {{", data.Name);
                foreach (var p in data.Request.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("         {");
                        f.WriteLine("            {0} = new List<{1}>();", GetAccessorName(p.Name), GetType(p.Type));
                        f.WriteLine("            int c = cdefMessage.GetInt();");
                        f.WriteLine("            for (int i=0; i<c; i++) {");
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("               {0}.Add(new {1}(cdefMessage));", GetAccessorName(p.Name), p.Type);
                        }
                        else
                        {
                            f.WriteLine("               {0}.Add({1});", GetAccessorName(p.Name),
                                        GetCdefGet(GetType(p.Type)));
                        }
                        f.WriteLine("            }");
                        f.WriteLine("         }");

                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("         {0} = new {1}(cdefMessage);", GetAccessorName(p.Name), p.Type);
                        }
                        else
                        {
                            f.WriteLine("         {0} = {1};", GetAccessorName(p.Name), GetCdefGet(GetType(p.Type)));
                        }
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                // Encode
                f.WriteLine("      public override void Encode(CdefMessage cdefMessage) {");
                f.WriteLine("         base.Encode(cdefMessage);");
                foreach (var p in data.Request.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("         cdefMessage.PutInt({0}.Count);", GetAccessorName(p.Name));
                        f.WriteLine("         foreach (var obj in {0}) {{", GetAccessorName(p.Name));
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("            obj.Encode(cdefMessage);");
                        }
                        else
                        {
                            f.WriteLine("            {0};", GetCdefPut(GetType(p.Type), "obj"));
                        }
                        f.WriteLine("         }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("         {0}.Encode(cdefMessage);", GetAccessorName(p.Name));
                        }
                        else
                        {
                            f.WriteLine("         {0};", GetCdefPut(GetType(p.Type), GetAccessorName(p.Name)));
                        }
                    }
                }
                f.WriteLine("      }");

                // Create response
                if (data.Response != null)
                {
                    f.WriteLine(""); 
                    f.WriteLine("      public {0}Response CreateResponse() {{", data.Name);
                    f.WriteLine("         {0}Response response = new {0}Response(TransactionNumber);", data.Name);
                    f.WriteLine("         return response;");
                    f.WriteLine("      }");
                }

                f.WriteLine("   }");
                f.WriteLine("}");
                f.Close();
            }
        }

        private void HandleClass(Class data)
        {
            var hasLists = false;

            var f = HandlerSupport.OpenNetFilename("Classes", data.Name, ".cs");
            f.WriteLine("using System;");
            f.WriteLine("using System.Collections.Generic;");
            f.WriteLine("using hasoftware.Cdef;");
            f.WriteLine("using {0}.Api;", Specification.Namespace);
            f.WriteLine("");
            f.WriteLine("namespace {0}.Classes {{", Specification.Namespace);
            f.WriteLine("   public class {0} {{", GetAccessorName(data.Name));

            // Attributes
            foreach (var a in data.Attributes.AttributeList)
            {
                if (a.IsList)
                {
                    hasLists = true;
                    f.WriteLine("      public List<{0}> {1} {{ get; set; }}", GetType(a.Type), GetAccessorName(a.Name));
                }
                else
                {
                    f.WriteLine("      public {0} {1} {{ get; set; }}", GetType(a.Type), GetAccessorName(a.Name));
                }
            }
            f.WriteLine("");

            // Default constructor
            f.WriteLine("      public {0}() {{", GetAccessorName(data.Name));
            foreach (var a in data.Attributes.AttributeList)
            {
                if (a.IsList)
                {
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("         {0} = new List<{1}>();", GetAccessorName(a.Name), GetType(a.Type));
                    }
                    else
                    {
                        throw new ApplicationException("TODO List attribute of non class type");
                    }
                }
            }
            f.WriteLine("      }");
            f.WriteLine("");

            // CdefMessage constructor
            f.WriteLine("      public {0}(CdefMessage cdefMessage) {1}{{", GetAccessorName(data.Name),
                        (hasLists ? ": this() " : ""));
            foreach (var a in data.Attributes.AttributeList)
            {
                if (a.IsList)
                {
                    f.WriteLine("         {");
                    f.WriteLine("            var c = cdefMessage.GetInt();");
                    f.WriteLine("            for (var i=0; i<c; i++) {");
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("               {0}.Add(new {1}(cdefMessage));", GetAccessorName(a.Name), a.Type);
                    }
                    else
                    {
                        throw new ApplicationException("TODO List attribute of non class type");
                    }
                    f.WriteLine("            }");
                    f.WriteLine("         }");
                }
                else
                {
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("         {0} = new {1}(cdefMessage);", GetAccessorName(a.Name), a.Type);
                    }
                    else
                    {
                        f.WriteLine("         {0} = {1};", GetAccessorName(a.Name), GetCdefGet(GetType(a.Type)));
                    }
                }
            }
            f.WriteLine("      }");
            f.WriteLine("");

            // Encode
            f.WriteLine("      public void Encode(CdefMessage cdefMessage) {");
            foreach (var a in data.Attributes.AttributeList)
            {
                if (a.IsList)
                {
                    f.WriteLine("         cdefMessage.PutInt({0}.Count);", GetAccessorName(a.Name));
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("         foreach (var obj in {0}) {{ obj.Encode(cdefMessage); }}",
                                    GetAccessorName(a.Name));
                    }
                    else
                    {
                        throw new ApplicationException("TODO List attribute of non class type");
                    }
                }
                else
                {
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("         {0}.Encode(cdefMessage);", GetAccessorName(a.Name));
                    }
                    else
                    {
                        f.WriteLine("         {0};", GetCdefPut(GetType(a.Type), GetAccessorName(a.Name)));
                    }
                }
            }
            f.WriteLine("      }");
            f.WriteLine("   }");
            f.WriteLine("}");
            f.Close();
        }

        private void HandleConstants()
        {
            var f = HandlerSupport.OpenNetFilename("Api", "FunctionCode", ".cs");
            f.WriteLine("using hasoftware.Cdef;");
            f.WriteLine("");
            f.WriteLine("namespace {0}.Api {{", Specification.Namespace);
            f.WriteLine("   public class FunctionCode : CdefFunctionCode {");
            foreach (var m in Specification.Messages.MessageList)
            {
                if (!string.IsNullOrEmpty(m.Code))
                {
                    f.WriteLine("      public const int " + m.Name + " = " + m.Code + ";");
                }
            }
            f.WriteLine("   }");
            f.WriteLine("}");
            f.Close();

            f = HandlerSupport.OpenNetFilename("Api", "MessageFactory", ".cs");
            f.WriteLine("using hasoftware.Cdef;");
            f.WriteLine("using hasoftware.Messages;");
            f.WriteLine("");
            f.WriteLine("namespace {0}.Api {{", Specification.Namespace);
            f.WriteLine("   public class MessageFactory {");
            f.WriteLine("      public static Message Decode(CdefMessage cdefMessage) {");
            f.WriteLine("         Message message = null;");
            f.WriteLine("         int functionCode = cdefMessage.GetInt(0);");
            f.WriteLine("         int transactionNumber = cdefMessage.GetInt();");
            f.WriteLine("         int systemFlags = cdefMessage.GetInt();");
            f.WriteLine("         if (systemFlags == (CdefSystemFlags.Error | CdefSystemFlags.Response)) {");
            f.WriteLine("            message = new ErrorResponse(cdefMessage);");
            f.WriteLine("         } else {");
            f.WriteLine("            bool isRequest = ((systemFlags & CdefSystemFlags.Response) == 0);");
            f.WriteLine("            switch (functionCode) {");
            foreach (var m in Specification.Messages.MessageList)
            {
                if (!string.IsNullOrEmpty(m.Code))
                {
                    f.WriteLine("               case FunctionCode.{0}:", m.Name);
                    if (m.Request != null)
                    {
                        f.WriteLine("                  if (isRequest) message = new {0}Request(cdefMessage);", m.Name);
                    }
                    if (m.Response != null)
                    {
                        f.WriteLine("                  if (!isRequest) message = new {0}Response(cdefMessage);", m.Name);
                    }
                    
                    f.WriteLine("                  break;");
                }
            }
            f.WriteLine("            }");
            f.WriteLine("         }");
            f.WriteLine("         return message;");
            f.WriteLine("      }");
            f.WriteLine("   }");
            f.WriteLine("}");
            f.Close();
        }

        private static string GetAccessorName(string data)
        {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".IndexOf(data[0]) == -1)
            {
                data = data.Substring(0, 1).ToUpper() + data.Substring(1);
            }
            return data;
        }

        private string GetCdefGet(string data)
        {
            return string.Format("cdefMessage.Get{0}()", GetType(data, true));
        }

        private string GetCdefPut(string data, string variable)
        {
            return string.Format("cdefMessage.Put{0}({1})", GetType(data, true), variable);
        }

        private string GetType(string data, bool cdef = false)
        {
            if (data.IndexOf('.') != -1)
            {
                // TODO Check class and attribute exist
                var c = data.Substring(0, data.IndexOf('.'));
                var a = data.Substring(data.IndexOf('.') + 1);
                data = Specification.Classes.GetClass(c).Attributes.GetAttribute(a).Type;
            }

            var test1 = data.ToLower();
            switch (test1)
            {
                case "int":
                    return cdef ? "Int" : "int";

                case "long":
                    return cdef ? "Long" : "long";

                case "string":
                    return cdef ? "String" : "string";

                case "boolean":
                case "bool":
                    return cdef ? "Boolean" : "bool";

                case "date":
                case "datetime":
                    return "DateTime";

                case "guid":
                case "uuid":
                case "oid":
                    return "Guid";
            }

            var cl = Specification.Classes.GetClass(GetAccessorName(data));
            if (cl != null) return GetAccessorName(data);

            throw new ApplicationException("Unknown type " + data);
        }
    }
}
