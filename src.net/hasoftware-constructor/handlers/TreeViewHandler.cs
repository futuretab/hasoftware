using System.Windows.Forms;
using hasoftware.model;
using Attribute = hasoftware.model.Attribute;
using Message = hasoftware.model.Message;

namespace hasoftware.handlers
{
    class TreeViewHandler : IHandler
    {
        public TreeView TreeView { protected get; set; }

        public void Handle(Specification specification)
        {
            TreeView.BeginUpdate();
            TreeView.Nodes.Clear();
            AddNode(TreeView, specification);
            TreeView.EndUpdate();
        }

        private void AddNode(TreeView parent, Specification data)
        {
            var index = parent.Nodes.Add(new TreeNode(Specification.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
            AddNode(node, data.Database);
            AddNode(node, data.Classes);
            AddNode(node, data.Messages);
        }

        private void AddNode(TreeNode parent, Database data)
        {
            if (data == null) return;
            var index = parent.Nodes.Add(new TreeNode(Database.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
            AddNode(node, data.Tables);
        }

        private void AddNode(TreeNode parent, Tables data)
        {
            var index = parent.Nodes.Add(new TreeNode(Tables.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
            foreach (var t in data.TableList)
            {
                AddNode(node, t);
            }
        }

        private void AddNode(TreeNode parent, Table data)
        {
            var index = parent.Nodes.Add(new TreeNode(data.Name));
            var node = parent.Nodes[index];
            node.Tag = data;
            AddNode(node, data.Columns);
        }

        private void AddNode(TreeNode parent, Columns data)
        {
            var index = parent.Nodes.Add(new TreeNode(Columns.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
            foreach (var c in data.ColumnList)
            {
                AddNode(node, c);
            }
        }

        private void AddNode(TreeNode parent, Column data)
        {
            var index = parent.Nodes.Add(new TreeNode(data.Name));
            var node = parent.Nodes[index];
            node.Tag = data;
        }

        private void AddNode(TreeNode parent, Classes data)
        {
            var index = parent.Nodes.Add(new TreeNode(Classes.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
            foreach (var c in data.ClassList)
            {
                AddNode(node, c);
            }
        }

        private void AddNode(TreeNode parent, Class data)
        {
            var index = parent.Nodes.Add(new TreeNode(data.Name));
            var node = parent.Nodes[index];
            node.Tag = data;
            AddNode(node, data.Attributes);
        }

        private void AddNode(TreeNode parent, Attributes data)
        {
            var index = parent.Nodes.Add(new TreeNode(Attributes.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
            foreach (var a in data.AttributeList)
            {
                AddNode(node, a);
            }
        }

        private void AddNode(TreeNode parent, Attribute data)
        {
            var index = parent.Nodes.Add(new TreeNode(data.Name));
            var node = parent.Nodes[index];
            node.Tag = data;
        }

        private void AddNode(TreeNode parent, Messages data)
        {
            var index = parent.Nodes.Add(new TreeNode(Messages.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
            foreach (var m in data.MessageList)
            {
                AddNode(node, m);
            }
        }

        private void AddNode(TreeNode parent, Message data)
        {
            var index = parent.Nodes.Add(new TreeNode(data.Name));
            var node = parent.Nodes[index];
            node.Tag = data;
            if (data.Request != null) AddNode(node, data.Request);
            if (data.Response != null) AddNode(node, data.Response);
        }

        private void AddNode(TreeNode parent, Request data)
        {
            var index = parent.Nodes.Add(new TreeNode(Request.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
        }

        private void AddNode(TreeNode parent, Response data)
        {
            var index = parent.Nodes.Add(new TreeNode(Response.CName));
            var node = parent.Nodes[index];
            node.Tag = data;
        }
    }
}
