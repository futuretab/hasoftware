using System;
using hasoftware.model;

namespace hasoftware.handlers
{
    public class JavaCodeHandler : IHandler
    {
        protected Specification Specification;

        public void Handle(Specification specification)
        {
            Specification = specification;
            HandleClasses();
            HandleMessages();
            HandleConstants();
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
            if (data.Request != null)
            {
                var f = HandlerSupport.OpenJavaFilename("messages", data.Name + "Request", ".java");
                f.WriteLine("package {0}.api.messages;", Specification.Namespace);
                f.WriteLine("");
                f.WriteLine("import {0}.api.FunctionCode;", Specification.Namespace);
                f.WriteLine("import {0}.api.classes.*;", Specification.Namespace);
                f.WriteLine("import hasoftware.api.Message;");
                f.WriteLine("import hasoftware.cdef.*;");
                f.WriteLine("import java.util.LinkedList;");
                f.WriteLine("import java.util.List;");
                f.WriteLine("");
                f.WriteLine("public class {0}Request extends Message {{", data.Name);

                // Attributes
                foreach (var p in data.Request.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("   private List<{0}> _{1} = new LinkedList<>();", GetCollectionType(p.Type), p.Name);
                    }
                    else
                    {
                        f.WriteLine("   private {0} _{1};", GetType(p.Type), p.Name);
                    }
                }
                if (data.Request.ParameterList.Count != 0) { f.WriteLine(""); }

                // Default constructor
                f.WriteLine("   public {0}Request() {{", data.Name);
                f.WriteLine("      super(FunctionCode.{0}, 0);", data.Name);
                f.WriteLine("   }");
                f.WriteLine("");

                // CdefMessage Constructor
                f.WriteLine("   public {0}Request(CDEFMessage cdefMessage) {{", data.Name);
                f.WriteLine("      super(cdefMessage);");
                foreach (var p in data.Request.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("      {");
                        f.WriteLine("         int c = cdefMessage.getInt();");
                        f.WriteLine("         for (int i=0; i<c; i++) {");
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("            _{0}.add(new {1}(cdefMessage));", p.Name, GetType(p.Type));
                        }
                        else
                        {
                            f.WriteLine("            _{0}.add({1});", p.Name, GetCdefGet(GetType(p.Type)));
                        }
                        f.WriteLine("         }");
                        f.WriteLine("      }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("      _{0} = new {1}(cdefMessage);", p.Name, GetType(p.Type));
                        }
                        else
                        {
                            f.WriteLine("      _{0} = {1};", p.Name, GetCdefGet(GetType(p.Type)));
                        }
                    }
                }
                f.WriteLine("   }");
                f.WriteLine("");

                // Encode
                f.WriteLine("   @Override");
                f.WriteLine("   public void encode(CDEFMessage cdefMessage) {");
                f.WriteLine("      super.encode(cdefMessage);");
                foreach (var p in data.Request.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("      cdefMessage.putInt(_{0}.size());", p.Name);
                        f.WriteLine("      for ({0} obj : _{1}) {{", GetCollectionType(p.Type), p.Name);
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("         obj.encode(cdefMessage);");
                        }
                        else
                        {
                            f.WriteLine("         {0};", GetCdefPut(GetType(p.Type), "obj", ""));
                        }
                        f.WriteLine("      }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("      {0}.encode(cdefMessage);", p.Name);
                        }
                        else
                        {
                            f.WriteLine("      {0};", GetCdefPut(GetType(p.Type), p.Name));
                        }
                    }
                }
                f.WriteLine("   }");

                // Create response
                if (data.Response != null)
                {
                    f.WriteLine("");
                    f.WriteLine("   public {0}Response createResponse() {{", data.Name);
                    f.WriteLine("      {0}Response response = new {0}Response(getTransactionNumber());", data.Name);
                    f.WriteLine("      return response;");
                    f.WriteLine("   }");
                }

                // Accessors
                foreach (var p in data.Request.ParameterList)
                {
                    f.WriteLine("");
                    if (p.IsList)
                    {
                        f.WriteLine("   public List<{0}> get{1}() {{ return _{2}; }}", GetCollectionType(p.Type), GetAccessorName(p.Name), p.Name);
                        f.WriteLine("   public void set{1}(List<{0}> {2}) {{ _{2} = {2}; }}", GetCollectionType(p.Type), GetAccessorName(p.Name), p.Name);
                    }
                    else
                    {
                        f.WriteLine("   public {0} get{1}() {{ return _{2}; }}", GetType(p.Type), GetAccessorName(p.Name), p.Name);
                        f.WriteLine("   public void set{1}({0} {2}) {{ _{2} = {2}; }}", GetType(p.Type), GetAccessorName(p.Name), p.Name);
                    }
                }

                f.WriteLine("}");
                f.Close();
            }
            if (data.Response != null)
            {
                var f = HandlerSupport.OpenJavaFilename("messages", data.Name + "Response", ".java");
                f.WriteLine("package {0}.api.messages;", Specification.Namespace);
                f.WriteLine("");
                f.WriteLine("import {0}.api.FunctionCode;", Specification.Namespace);
                f.WriteLine("import {0}.api.classes.*;", Specification.Namespace);
                f.WriteLine("import hasoftware.api.Message;");
                f.WriteLine("import hasoftware.cdef.*;");
                f.WriteLine("import java.util.LinkedList;");
                f.WriteLine("import java.util.List;");
                f.WriteLine("");
                f.WriteLine("public class {0}Response extends Message {{", data.Name);

                // Attributes
                foreach (var p in data.Response.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("   private List<{0}> _{1} = new LinkedList<>();", GetCollectionType(p.Type), p.Name);
                    }
                    else
                    {
                        f.WriteLine("   private {0} _{1};", GetType(p.Type), p.Name);
                    }
                }
                if (data.Response.ParameterList.Count != 0) { f.WriteLine(""); }

                // Default constructor
                f.WriteLine("   public {0}Response(int transactionNumber) {{", data.Name);
                f.WriteLine("      super(FunctionCode.{0}, transactionNumber, CDEFSystemFlags.Response);", data.Name);
                f.WriteLine("   }");
                f.WriteLine("");

                // CdefMessage Constructor
                f.WriteLine("   public {0}Response(CDEFMessage cdefMessage) {{", data.Name);
                f.WriteLine("      super(cdefMessage);");
                foreach (var p in data.Response.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("      {");
                        f.WriteLine("         int c = cdefMessage.getInt();");
                        f.WriteLine("         for (int i=0; i<c; i++) {");
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("            _{0}.add(new {1}(cdefMessage));", p.Name, GetType(p.Type));
                        }
                        else
                        {
                            f.WriteLine("            _{0}.add({1});", p.Name, GetCdefGet(GetType(p.Type)));
                        }
                        f.WriteLine("         }");
                        f.WriteLine("      }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("      _{0} = new {1}(cdefMessage);", p.Name, GetType(p.Type));
                        }
                        else
                        {
                            f.WriteLine("      _{0} = {1};", p.Name, GetCdefGet(GetType(p.Type)));
                        }
                    }
                }
                f.WriteLine("   }");
                f.WriteLine("");

                // Encode
                f.WriteLine("   @Override");
                f.WriteLine("   public void encode(CDEFMessage cdefMessage) {");
                f.WriteLine("      super.encode(cdefMessage);");
                foreach (var p in data.Response.ParameterList)
                {
                    if (p.IsList)
                    {
                        f.WriteLine("      cdefMessage.putInt(_{0}.size());", p.Name);
                        f.WriteLine("      for ({0} obj : _{1}) {{", GetCollectionType(p.Type), p.Name);
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("         obj.encode(cdefMessage);");
                        }
                        else
                        {
                            f.WriteLine("         {0};", GetCdefPut(GetType(p.Type), "obj", ""));
                        }
                        f.WriteLine("      }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(p.Type))
                        {
                            f.WriteLine("      {0}.encode(cdefMessage);", p.Name);
                        }
                        else
                        {
                            f.WriteLine("      {0};", GetCdefPut(GetType(p.Type), p.Name));
                        }
                    }
                }
                f.WriteLine("   }");

                // Accessors
                foreach (var p in data.Response.ParameterList)
                {
                    f.WriteLine("");
                    if (p.IsList)
                    {
                        f.WriteLine("   public List<{0}> get{1}() {{ return _{2}; }}", GetCollectionType(p.Type), GetAccessorName(p.Name), p.Name);
                        f.WriteLine("   public void set{1}(List<{0}> {2}) {{ _{2} = {2}; }}", GetCollectionType(p.Type), GetAccessorName(p.Name), p.Name);
                    }
                    else
                    {
                        f.WriteLine("   public {0} get{1}() {{ return _{2}; }}", GetType(p.Type), GetAccessorName(p.Name), p.Name);
                        f.WriteLine("   public void set{1}({0} {2}) {{ _{2} = {2}; }}", GetType(p.Type), GetAccessorName(p.Name), p.Name);
                    }
                }

                f.WriteLine("}");
                f.Close();
            }
        }

        private void HandleConstants()
        {
            var f = HandlerSupport.OpenJavaFilename("FunctionCode", ".java");
            f.WriteLine("package {0}.api;", Specification.Namespace);
            f.WriteLine("");
            f.WriteLine("import hasoftware.cdef.CDEFFunctionCode;");
            f.WriteLine("");
            f.WriteLine("public class FunctionCode extends CDEFFunctionCode {");
            foreach (var m in Specification.Messages.MessageList)
            {
                if (!string.IsNullOrEmpty(m.Code))
                {
                    f.WriteLine("      public static final int " + m.Name + " = " + m.Code + ";");
                }
            }
            f.WriteLine("}");
            f.Close();

            f = HandlerSupport.OpenJavaFilename("CDEFMessageFactory", ".java");
            f.WriteLine("package {0}.api;", Specification.Namespace);
            f.WriteLine("");
            foreach (var m in Specification.Messages.MessageList)
            {
                if (!string.IsNullOrEmpty(m.Code))
                {
                    if (m.Request != null)
                    {
                        f.WriteLine("import {0}.api.messages.{1}Request;", Specification.Namespace, m.Name);
                    }
                    if (m.Response != null)
                    {
                        f.WriteLine("import {0}.api.messages.{1}Response;", Specification.Namespace, m.Name);
                    }
                }
            }
            f.WriteLine("import hasoftware.cdef.CDEFMessage;");
            f.WriteLine("import hasoftware.cdef.CDEFSystemFlags;");
            f.WriteLine("");
            f.WriteLine("public class CDEFMessageFactory {");
            f.WriteLine("   public static Message decode(CDEFMessage cdefMessage) {");
            f.WriteLine("      Message message = null;");
            f.WriteLine("      int functionCode = cdefMessage.getInt(0);");
            f.WriteLine("      int transactionNumber = cdefMessage.getInt();");
            f.WriteLine("      int systemFlags = cdefMessage.getInt();");
            f.WriteLine("      if (systemFlags == (CDEFSystemFlags.Error | CDEFSystemFlags.Response)) {");
            f.WriteLine("         message = new ErrorResponse(cdefMessage);");
            f.WriteLine("      } else {");
            f.WriteLine("         boolean isRequest = ((systemFlags & CDEFSystemFlags.Response) == 0);");
            f.WriteLine("         switch (functionCode) {");
            foreach (var m in Specification.Messages.MessageList)
            {
                if (!string.IsNullOrEmpty(m.Code))
                {
                    f.WriteLine("            case FunctionCode.{0}:", m.Name);
                    if (m.Request != null)
                    {
                        f.WriteLine("               if (isRequest) message = new {0}Request(cdefMessage);", m.Name);
                    }
                    if (m.Response != null)
                    {
                        f.WriteLine("               if (!isRequest) message = new {0}Response(cdefMessage);", m.Name);
                    }
                    f.WriteLine("               break;");
                }
            }
            f.WriteLine("         }");
            f.WriteLine("      }");
            f.WriteLine("      return message;");
            f.WriteLine("   }");
            f.WriteLine("}");
            f.Close();
        }

        private void HandleClasses()
        {
            foreach (var c in Specification.Classes.ClassList)
            {
                HandleClass(c);
            }
        }

        private void HandleClass(Class data)
        {
            var f = HandlerSupport.OpenJavaFilename("classes", data.Name, ".java");
            f.WriteLine("package {0}.api.classes;", Specification.Namespace);
            f.WriteLine("");
            f.WriteLine("import hasoftware.cdef.CDEFMessage;");
            f.WriteLine("import java.util.List;");
            f.WriteLine("import java.util.LinkedList;");
            f.WriteLine("");
            f.WriteLine("public class {0} {{", GetAccessorName(data.Name));

            // Attributes
            foreach (var a in data.Attributes.AttributeList)
            {
                if (a.IsList)
                {
                    f.WriteLine("   private List<{0}> _{1} = new LinkedList<>();", GetType(a.Type), a.Name);
                }
                else
                {
                    f.WriteLine("   private {0} _{1};", GetType(a.Type), a.Name);
                }
            }
            f.WriteLine("");

            // Default constructor
            f.WriteLine("   public {0}() {{", GetAccessorName(data.Name));
            f.WriteLine("   }");
            f.WriteLine("");

            // CdefMessage constructor
            f.WriteLine("   public {0}(CDEFMessage cdefMessage) {{", GetAccessorName(data.Name));
            foreach (var a in data.Attributes.AttributeList)
            {
                if (a.IsList)
                {
                    f.WriteLine("      {");
                    f.WriteLine("         int c = cdefMessage.getInt();");
                    f.WriteLine("         for (int i=0; i<c; i++) {");
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("            _{0}.add(new {1}(cdefMessage));", a.Name, a.Type);
                    }
                    else
                    {
                        throw new ApplicationException("TODO List attribute of non class type");
                    }
                    f.WriteLine("         }");
                    f.WriteLine("      }");
                }
                else
                {
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("      _{0} = new {1}(cdefMessage);", a.Name, a.Type);
                    }
                    else
                    {
                        f.WriteLine("      _{0} = {1};", a.Name, GetCdefGet(GetType(a.Type)));
                    }
                }
            }
            f.WriteLine("   }");
            f.WriteLine("");

            // Encode
            f.WriteLine("   public void encode(CDEFMessage cdefMessage) {");
            foreach (var a in data.Attributes.AttributeList)
            {
                if (a.IsList)
                {
                    f.WriteLine("      cdefMessage.putInt(_{0}.size());", a.Name);
                    if (Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("      for ({0} obj : _{1}) {{ obj.encode(cdefMessage); }}", GetType(a.Type), a.Name);
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
                        f.WriteLine("      _{0}.encode(cdefMessage);", a.Name);
                    }
                    else
                    {
                        f.WriteLine("      {0};", GetCdefPut(GetType(a.Type), a.Name));
                    }
                }
            }
            f.WriteLine("   }");

            // Accessors
            foreach (var a in data.Attributes.AttributeList)
            {
                f.WriteLine("");
                if (a.IsList)
                {
                    f.WriteLine("   public List<{0}> get{1}() {{ return _{2}; }}", GetCollectionType(a.Type), GetAccessorName(a.Name), a.Name);
                    f.WriteLine("   public void set{1}(List<{0}> {2}) {{ _{2} = {2}; }}", GetCollectionType(a.Type), GetAccessorName(a.Name), a.Name);
                }
                else
                {
                    f.WriteLine("   public {0} get{1}() {{ return _{2}; }}", GetType(a.Type), GetAccessorName(a.Name), a.Name);
                    f.WriteLine("   public void set{1}({0} {2}) {{ _{2} = {2}; }}", GetType(a.Type), GetAccessorName(a.Name), a.Name);
                }
            }

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
            return string.Format("cdefMessage.get{0}()", GetType(data, true));
        }

        private string GetCdefPut(string data, string variable, string prefix = "_")
        {
            return string.Format("cdefMessage.put{0}({2}{1})", GetType(data, true), variable, prefix);
        }

        private string GetCollectionType(string data)
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
                    return "Integer";

                case "long":
                    return "Long";

                case "string":
                    return "String";
            }

            var cl = Specification.Classes.GetClass(GetAccessorName(data));
            if (cl != null) return GetAccessorName(data);

            throw new ApplicationException("Unknown type " + data);
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
                    return cdef ? "String" : "String";

                case "boolean":
                case "bool":
                    return cdef ? "Boolean" : "boolean";

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
