using hasoftware.model;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace hasoftware.handlers
{
    class CppCodeHandler: IHandler
    {
        protected Specification Specification;

        public void Handle(Specification specification)
        {
            Specification = specification;
            HandleClasses();
            HandleMessages();
            HandleConstants();
        }

        // ****************************************************************
        // MESSAGES
        // ****************************************************************
        private void HandleMessage(StreamWriter f, string name, List<Parameter> parameters, string id, string type)
        {
            f.WriteLine("   class {0} : public HASoftware::CdefMessageBase {{", GetAccessorName(name));
            f.WriteLine("   private:");

            // Attributes
            foreach (var p in parameters)
            {
                if (p.IsList)
                {
                    var extra = (Specification.Classes.IsClass(p.Type)) ? "*" : "";
                    f.WriteLine("      std::list<{0}{1}> _{2};", GetType(p.Type), extra, p.Name);
                }
                else
                {
                    f.WriteLine("      {0} _{1};", GetType(p.Type), p.Name);
                }
            }

            f.WriteLine("   public:");

            // Default constructor
            f.WriteLine("      {0}() : HASoftware::CdefMessageBase(FUNCTION_CODE_{1}, 0, CDEF_SYSTEM_FLAGS_{2}) {{", GetAccessorName(name), id, type);
            foreach (var p in parameters)
            {
                if (p.Default.Length != 0)
                {
                    f.WriteLine("         _{0} = {1};", p.Name, p.Default);
                }
            }
            f.WriteLine("      }");
            f.WriteLine("");

            // CdefMessage constructor
            f.WriteLine("      {0}(HASoftware::CdefMessage &cdefMessage) : HASoftware::CdefMessageBase(cdefMessage) {{", GetAccessorName(name));
            foreach (var p in parameters)
            {
                if (p.IsList)
                {
                    f.WriteLine("         {");
                    f.WriteLine("            int count = cdefMessage.GetInt();");
                    f.WriteLine("            while (count > 0) {");
                    if (Specification.Classes.IsClass(p.Type))
                    {
                        f.WriteLine("               _{0}.push_back(new {1}(cdefMessage));", p.Name, p.Type);
                    }
                    else
                    {
                        f.WriteLine("               _{0}.push_back({1});", p.Name, GetCdefGet(GetType(p.Type)));
                    }
                    f.WriteLine("               count--;");
                    f.WriteLine("            }");
                    f.WriteLine("         }");
                }
                else
                {
                    if (Specification.Classes.IsClass(p.Type))
                    {
                        f.WriteLine("         _{0} = new {1}(cdefMessage);", p.Name, p.Type);
                    }
                    else
                    {
                        f.WriteLine("         _{0} = {1};", p.Name, GetCdefGet(GetType(p.Type)));
                    }
                }
            }
            f.WriteLine("      }");
            f.WriteLine("");

            // Destructor
            f.WriteLine("      virtual ~{0}() {{", GetAccessorName(name));
            foreach (var p in parameters)
            {
                if (p.IsList && Specification.Classes.IsClass(p.Type))
                {
                    f.WriteLine("         while (_{0}.size() != 0) {{ delete _{0}.front(); _{0}.pop_front(); }}", p.Name);
                }
            }
            f.WriteLine("      }");
            f.WriteLine("");

            // Accessors
            foreach (var p in parameters)
            {
                if (p.IsList)
                {
                    var extra = (Specification.Classes.IsClass(p.Type)) ? "*" : "";
                    f.WriteLine("      std::list<{0}{1}>& Get{2}() {{ return _{3}; }}", GetType(p.Type), extra, GetAccessorName(p.Name), p.Name);
                    // TODO Set method required ?
                    //f.WriteLine("      void set{1}(List<{0}> {2}) {{ _{2} = {2}; }}", GetCollectionType(a.Type), GetAccessorName(a.Name), a.Name);
                }
                else
                {
                    f.WriteLine("      {0} Get{1}() {{ return _{2}; }}", GetType(p.Type), GetAccessorName(p.Name), p.Name);
                    f.WriteLine("      void Set{1}({0} {2}) {{ _{2} = {2}; }}", GetType(p.Type), GetAccessorName(p.Name), p.Name);
                }
                f.WriteLine("");
            }

            // Encode
            f.WriteLine("      virtual void Encode(HASoftware::CdefMessage &cdefMessage) {");
            f.WriteLine("         HASoftware::CdefMessageBase::Encode(cdefMessage);");
            foreach (var p in parameters)
            {
                if (p.IsList)
                {
                    f.WriteLine("         cdefMessage.PutInt(_{0}.size());", p.Name);
                    if (Specification.Classes.IsClass(p.Type))
                    {
                        f.WriteLine("         for (std::list<{0}*>::iterator it=_{1}.begin(); it!=_{1}.end(); ++it) {{", GetType(p.Type), p.Name);
                        f.WriteLine("            (*it)->Encode(cdefMessage);");
                        f.WriteLine("         }");
                    }
                    else
                    {
                        f.WriteLine("         for (std::list<{0}>::iterator it=_{1}.begin(); it!=_{1}.end(); ++it) {{", GetType(p.Type), p.Name);
                        f.WriteLine("            {0};", GetCdefPut(GetType(p.Type), "*it"));
                        f.WriteLine("         }");
                    }
                }
                else
                {
                    if (Specification.Classes.IsClass(p.Type))
                    {
                        f.WriteLine("         _{0}.Encode(cdefMessage);", p.Name);
                    }
                    else
                    {
                        f.WriteLine("         {0};", GetCdefPut(GetType(p.Type), "_" + p.Name));
                    }
                }
            }
            f.WriteLine("      }");
            f.WriteLine("");

            f.WriteLine("   };");
            f.WriteLine("");
        }

        private void HandleMessages()
        {
            var f = HandlerSupport.OpenCppFilename("Messages", ".h");
            f.WriteLine("#ifndef MESSAGES_H");
            f.WriteLine("#define MESSAGES_H");
            f.WriteLine("");
            f.WriteLine("#include <list>");
            f.WriteLine("#include <string>");
            f.WriteLine("#include <vector>");
            f.WriteLine("");
            f.WriteLine("#include \"CdefMessages.h\"");
            f.WriteLine("#include \"Classes.h\"");
            f.WriteLine("");
            f.WriteLine("namespace {0} {{", Specification.Namespace);
            f.WriteLine("");

            // ****************************************************************
            // FUNCTION CODES
            // ****************************************************************
            foreach (var m in Specification.Messages.MessageList)
            {
                if (!string.IsNullOrEmpty(m.Code))
                {
                    f.WriteLine("   #define FUNCTION_CODE_" + m.Name.ToUpper() + " " + m.Code);
                }
            }
            f.WriteLine("");

            // ****************************************************************
            // MESSAGES
            // ****************************************************************
            foreach (var m in Specification.Messages.MessageList)
            {
                if (m.Request != null)
                {
                    HandleMessage(f, m.Name + "Request", m.Request.ParameterList, m.Name.ToUpper(), "REQUEST");
                }
                if (m.Response != null)
                {
                    HandleMessage(f, m.Name + "Response", m.Response.ParameterList, m.Name.ToUpper(), "RESPONSE");
                }
            }
            f.WriteLine("}");
            f.WriteLine("");
            f.WriteLine("#endif");
            f.Close();
        }

        // ****************************************************************
        // CLASSES
        // ****************************************************************
        private void HandleClasses()
        {
            var f = HandlerSupport.OpenCppFilename("Classes", ".h");
            f.WriteLine("#ifndef CLASSES_H");
            f.WriteLine("#define CLASSES_H");
            f.WriteLine("");
            f.WriteLine("#include <list>");
            f.WriteLine("#include <string>");
            f.WriteLine("");
            f.WriteLine("#include \"CdefMessage.h\"");
            f.WriteLine("");
            f.WriteLine("namespace {0} {{", Specification.Namespace);
            f.WriteLine("");
            foreach (var c in Specification.Classes.ClassList)
            {
                f.WriteLine("   class {0} {{", GetAccessorName(c.Name));
                f.WriteLine("   private:");
                // Class attributes
                foreach (var a in c.Attributes.AttributeList)
                {
                    if (a.IsList)
                    {
                        var extra = (Specification.Classes.IsClass(a.Type)) ? "*" : "";
                        f.WriteLine("      std::list<{0}{1}> _{2};", GetType(a.Type), extra, a.Name);
                    }
                    else
                    {
                        f.WriteLine("      {0} _{1};", GetType(a.Type), a.Name);
                    }
                }
                f.WriteLine("");

                f.WriteLine("   public:");

                // Default constructor
                f.WriteLine("      {0}() {{", GetAccessorName(c.Name));
                foreach (var a in c.Attributes.AttributeList)
                {
                    if (a.Default.Length != 0)
                    {
                        f.WriteLine("         _{0} = {1};", a.Name, a.Default);
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                // CdefMessage constructor
                f.WriteLine("      {0}(HASoftware::CdefMessage &cdefMessage) {{", GetAccessorName(c.Name));
                foreach (var a in c.Attributes.AttributeList)
                {
                    if (a.IsList)
                    {
                        f.WriteLine("         {");
                        f.WriteLine("            int count = cdefMessage.GetInt();");
                        f.WriteLine("            while (count > 0) {");
                        if (Specification.Classes.IsClass(a.Type))
                        {
                            f.WriteLine("               _{0}.push_back(new {1}(cdefMessage));", a.Name, a.Type);
                        }
                        else
                        {
                            f.WriteLine("               _{0}.push_back({1});", a.Name, GetCdefGet(GetType(a.Type)));
                        }
                        f.WriteLine("               count--;");
                        f.WriteLine("            }");
                        f.WriteLine("         }");
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(a.Type))
                        {
                            f.WriteLine("         _{0} = {1}(cdefMessage);", a.Name, a.Type);
                        }
                        else
                        {
                            f.WriteLine("         _{0} = {1};", a.Name, GetCdefGet(GetType(a.Type)));
                        }
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                // Destructor
                f.WriteLine("      virtual ~{0}() {{", GetAccessorName(c.Name));
                foreach (var a in c.Attributes.AttributeList)
                {
                    if (a.IsList && Specification.Classes.IsClass(a.Type))
                    {
                        f.WriteLine("         while (_{0}.size() != 0) {{ delete _{0}.front(); _{0}.pop_front(); }}", a.Name);
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                // Accessors
                foreach (var a in c.Attributes.AttributeList)
                {
                    if (a.IsList)
                    {
                        var extra = (Specification.Classes.IsClass(a.Type)) ? "*" : "";
                        f.WriteLine("      std::list<{0}{1}>& Get{2}() {{ return _{3}; }}", GetType(a.Type), extra, GetAccessorName(a.Name), a.Name);
                        // TODO Set method required ?
                        //f.WriteLine("      void set{1}(List<{0}> {2}) {{ _{2} = {2}; }}", GetCollectionType(a.Type), GetAccessorName(a.Name), a.Name);
                    }
                    else
                    {
                        f.WriteLine("      {0} Get{1}() {{ return _{2}; }}", GetType(a.Type), GetAccessorName(a.Name), a.Name);
                        f.WriteLine("      void Set{1}({0} {2}) {{ _{2} = {2}; }}", GetType(a.Type), GetAccessorName(a.Name), a.Name);
                    }
                    f.WriteLine("");
                }

                // Encode
                f.WriteLine("      void Encode(HASoftware::CdefMessage &cdefMessage) {");
                foreach (var a in c.Attributes.AttributeList)
                {
                    if (a.IsList)
                    {
                        f.WriteLine("         cdefMessage.PutInt(_{0}.size());", a.Name);
                        if (Specification.Classes.IsClass(a.Type))
                        {
                            f.WriteLine("         for (std::list<{0}*>::iterator it=_{1}.begin(); it!=_{1}.end(); ++it) {{", GetType(a.Type), a.Name);
                            f.WriteLine("            (*it)->Encode(cdefMessage);");
                            f.WriteLine("         }");
                        }
                        else
                        {
                            f.WriteLine("         for (std::list<{0}>::iterator it=_{1}.begin(); it!=_{1}.end(); ++it) {{", GetType(a.Type), a.Name);
                            f.WriteLine("            {0};", GetCdefPut(GetType(a.Type), "*it"));
                            f.WriteLine("         }");
                        }
                    }
                    else
                    {
                        if (Specification.Classes.IsClass(a.Type))
                        {
                            f.WriteLine("         _{0}.Encode(cdefMessage);", a.Name);
                        }
                        else
                        {
                            f.WriteLine("         {0};", GetCdefPut(GetType(a.Type), "_" + a.Name));
                        }
                    }
                }
                f.WriteLine("      }");
                f.WriteLine("");

                f.WriteLine("   };");
                f.WriteLine("");
            }
            f.WriteLine("}");
            f.WriteLine("");
            f.WriteLine("#endif");
            f.Close();
        }

        private void HandleConstants()
        {
            // ****************************************************************
            // MESSAGE FACTORY
            // ****************************************************************
            var f = HandlerSupport.OpenCppFilename("MessageFactory", ".h");
            f.WriteLine("#ifndef MESSAGE_FACTORY_H");
            f.WriteLine("#define MESSAGE_FACTORY_H");
            f.WriteLine("");
            f.WriteLine("#include \"MessageBase.h\"");
            f.WriteLine("#include \"CdefMessage.h\"");
            f.WriteLine("");
            f.WriteLine("namespace {0} {{", Specification.Namespace);
            f.WriteLine("   class MessageFactory {");
            f.WriteLine("   public:");
            f.WriteLine("      static HASoftware::MessageBase* Decode(HASoftware::CdefMessage &cdefMessage);");
            f.WriteLine("    };");
            f.WriteLine("}");
            f.WriteLine("");
            f.WriteLine("#endif");
            f.Close();

            f = HandlerSupport.OpenCppFilename("MessageFactory", ".cpp");
            f.WriteLine("#include <stdlib.h>");
            f.WriteLine("");
            f.WriteLine("#include \"CdefMessages.h\"");
            f.WriteLine("#include \"FunctionCodes.h\"");
            f.WriteLine("#include \"MessageFactory.h\"");
            f.WriteLine("#include \"Messages.h\"");
            f.WriteLine("");
            f.WriteLine("using namespace HASoftware;");
            f.WriteLine("");
            f.WriteLine("namespace Lockheed {");
            f.WriteLine("   MessageBase* MessageFactory::Decode(CdefMessage &cdefMessage) {");
            f.WriteLine("      MessageBase* message = NULL;");
            f.WriteLine("      int functionCode = cdefMessage.GetInt(0);");
            f.WriteLine("      int transactionNumber = cdefMessage.GetInt();");
            f.WriteLine("      int systemFlags = cdefMessage.GetInt();");
            f.WriteLine("      if (systemFlags == (CdefSystemFlags::Error | CdefSystemFlags::Response)) {");
            f.WriteLine("         message = new ErrorResponse(cdefMessage);");
            f.WriteLine("      } else if (functionCode == CdefFunctionCode::Heartbeat && systemFlags == CdefSystemFlags::Request) {");
            f.WriteLine("         message = new HeartbeatRequest(cdefMessage);");
            f.WriteLine("      } else if (functionCode == CdefFunctionCode::Notify && systemFlags == CdefSystemFlags::Request) {");
            f.WriteLine("         message = new NotifyRequest(cdefMessage);");
            f.WriteLine("      } else if (functionCode == CdefFunctionCode::Notify && systemFlags == CdefSystemFlags::Response) {");
            f.WriteLine("         message = new NotifyResponse(cdefMessage);");
            f.WriteLine("      } else {");
            f.WriteLine("         bool isRequest = ((systemFlags & CdefSystemFlags::Response) == 0);");
            f.WriteLine("         switch (functionCode) {");
            foreach (var m in Specification.Messages.MessageList)
            {
                if (!string.IsNullOrEmpty(m.Code))
                {
                    f.WriteLine("         case FunctionCode::{0}:", m.Name);
                    if (m.Request != null)
                    {
                        f.WriteLine("            if (isRequest) message = new {0}Request(cdefMessage);", m.Name);
                    }
                    if (m.Response != null)
                    {
                        f.WriteLine("            if (!isRequest) message = new {0}Response(cdefMessage);", m.Name);
                    }
                    f.WriteLine("                break;");
                }
            }
            f.WriteLine("         }");
            f.WriteLine("      }");
            f.WriteLine("      return message;");
            f.WriteLine("    }");
            f.WriteLine("}");
            f.Close();
        }

        private string GetCdefGet(string data)
        {
            return string.Format("cdefMessage.Get{0}()", GetType(data, true));
        }

        private string GetCdefPut(string data, string variable)
        {
            return string.Format("cdefMessage.Put{0}({1})", GetType(data, true), variable);
        }

        private static string GetAccessorName(string data)
        {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".IndexOf(data[0]) == -1)
            {
                data = data.Substring(0, 1).ToUpper() + data.Substring(1);
            }
            return data;
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
                case "float":
                    return cdef ? "Float" : "float";

                case "double":
                    return cdef ? "Double" : "double";

                case "int":
                    return cdef ? "Int" : "int";

                case "long":
                    return cdef ? "Long" : "long";

                case "std::string":
                case "string":
                    return cdef ? "String" : "std::string";

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

                case "bytes":
                    return "std::vector<uint8_t>";

                case "std::vector<uint8_t>":
                    return "Bytes";
            }

            var cl = Specification.Classes.GetClass(GetAccessorName(data));
            if (cl != null) return GetAccessorName(data);

            throw new ApplicationException("Unknown type " + data);
        }
    }
}
