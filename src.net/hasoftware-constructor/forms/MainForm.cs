using System;
using System.IO;
using System.Windows.Forms;
using System.Xml;
using hasoftware.handlers;
using hasoftware.model;

namespace hasoftware.forms
{
    public partial class MainForm : Form
    {
        protected Specification Specification { get; set; }

        public MainForm()
        {
            InitializeComponent();
            _menuGenerate.Visible = false;
            _statusMessage.Text = "";
        }

        private void OnFileOpen(object sender, EventArgs e)
        {
            var ofd = new OpenFileDialog
            {
                Filter = @"Specification files (*.spec)|*.spec|All files (*.*)|*.*",
                DefaultExt = "*.spec"
            };
            if (ofd.ShowDialog() == DialogResult.OK)
            {
                var doc = new XmlDocument();
                try
                {
                    doc.Load(ofd.FileName);
                    foreach (XmlNode child in doc.ChildNodes)
                    {
                        if (child.NodeType == XmlNodeType.Element)
                        {
                            if (child.Name.Equals(Specification.CName))
                            {
                                Specification = new Specification(child);
                                var treeViewHandler = new TreeViewHandler { TreeView = _treeView };
                                treeViewHandler.Handle(Specification);
                            }
                        }
                    }
                    _menuGenerate.Visible = true;
                    HandlerSupport.Specification = Specification;
                    HandlerSupport.FileLocation = Path.GetDirectoryName(ofd.FileName);
                }
                catch
                {
                    MessageBox.Show(@"Exception loading specification file");
                }
            }
        }

        private void OnFileExit(object sender, EventArgs e)
        {
            Application.Exit();
        }

        private void OnGenerateCode(object sender, EventArgs e)
        {
            if (Specification == null) return;
            if (!string.IsNullOrEmpty(Specification.NetBaseDirectory))
            {
                var handler = new NetCodeHandler();
                handler.Handle(Specification);
            }
            if (!string.IsNullOrEmpty(Specification.JavaBaseDirectory))
            {
                var handler = new JavaCodeHandler();
                handler.Handle(Specification);
            }

            if (!string.IsNullOrEmpty(Specification.CppBaseDirectory))
            {
                var handler = new CppCodeHandler();
                handler.Handle(Specification);
            }
        }
    }
}
