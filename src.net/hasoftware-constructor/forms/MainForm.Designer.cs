namespace hasoftware.forms
{
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this._menu = new System.Windows.Forms.MenuStrip();
            this._menuFile = new System.Windows.Forms.ToolStripMenuItem();
            this._menuFileOpen = new System.Windows.Forms.ToolStripMenuItem();
            this._menuFileExit = new System.Windows.Forms.ToolStripMenuItem();
            this._menuGenerate = new System.Windows.Forms.ToolStripMenuItem();
            this._menuGenerateCode = new System.Windows.Forms.ToolStripMenuItem();
            this._menuGenerateSqlScripts = new System.Windows.Forms.ToolStripMenuItem();
            this._menuGenerateDocumentation = new System.Windows.Forms.ToolStripMenuItem();
            this._menuHelp = new System.Windows.Forms.ToolStripMenuItem();
            this._menuHelpAbout = new System.Windows.Forms.ToolStripMenuItem();
            this._treeView = new System.Windows.Forms.TreeView();
            this.label1 = new System.Windows.Forms.Label();
            this._status = new System.Windows.Forms.StatusStrip();
            this._statusMessage = new System.Windows.Forms.ToolStripStatusLabel();
            this._menu.SuspendLayout();
            this._status.SuspendLayout();
            this.SuspendLayout();
            // 
            // _menu
            // 
            this._menu.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this._menuFile,
            this._menuGenerate,
            this._menuHelp});
            this._menu.Location = new System.Drawing.Point(0, 0);
            this._menu.Name = "_menu";
            this._menu.Size = new System.Drawing.Size(784, 24);
            this._menu.TabIndex = 0;
            this._menu.Text = "menuStrip1";
            // 
            // _menuFile
            // 
            this._menuFile.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this._menuFileOpen,
            this._menuFileExit});
            this._menuFile.Name = "_menuFile";
            this._menuFile.Size = new System.Drawing.Size(40, 20);
            this._menuFile.Text = "FILE";
            // 
            // _menuFileOpen
            // 
            this._menuFileOpen.Name = "_menuFileOpen";
            this._menuFileOpen.Size = new System.Drawing.Size(105, 22);
            this._menuFileOpen.Text = "OPEN";
            this._menuFileOpen.Click += new System.EventHandler(this.OnFileOpen);
            // 
            // _menuFileExit
            // 
            this._menuFileExit.Name = "_menuFileExit";
            this._menuFileExit.Size = new System.Drawing.Size(105, 22);
            this._menuFileExit.Text = "EXIT";
            this._menuFileExit.Click += new System.EventHandler(this.OnFileExit);
            // 
            // _menuGenerate
            // 
            this._menuGenerate.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this._menuGenerateCode,
            this._menuGenerateSqlScripts,
            this._menuGenerateDocumentation});
            this._menuGenerate.Name = "_menuGenerate";
            this._menuGenerate.Size = new System.Drawing.Size(76, 20);
            this._menuGenerate.Text = "GENERATE";
            // 
            // _menuGenerateCode
            // 
            this._menuGenerateCode.Name = "_menuGenerateCode";
            this._menuGenerateCode.Size = new System.Drawing.Size(176, 22);
            this._menuGenerateCode.Text = "CODE";
            this._menuGenerateCode.Click += new System.EventHandler(this.OnGenerateCode);
            // 
            // _menuGenerateSqlScripts
            // 
            this._menuGenerateSqlScripts.Name = "_menuGenerateSqlScripts";
            this._menuGenerateSqlScripts.Size = new System.Drawing.Size(176, 22);
            this._menuGenerateSqlScripts.Text = "SQL SCRIPTS";
            // 
            // _menuGenerateDocumentation
            // 
            this._menuGenerateDocumentation.Name = "_menuGenerateDocumentation";
            this._menuGenerateDocumentation.Size = new System.Drawing.Size(176, 22);
            this._menuGenerateDocumentation.Text = "DOCUMENTATION";
            // 
            // _menuHelp
            // 
            this._menuHelp.Alignment = System.Windows.Forms.ToolStripItemAlignment.Right;
            this._menuHelp.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this._menuHelpAbout});
            this._menuHelp.Name = "_menuHelp";
            this._menuHelp.Size = new System.Drawing.Size(47, 20);
            this._menuHelp.Text = "HELP";
            // 
            // _menuHelpAbout
            // 
            this._menuHelpAbout.Name = "_menuHelpAbout";
            this._menuHelpAbout.Size = new System.Drawing.Size(113, 22);
            this._menuHelpAbout.Text = "ABOUT";
            // 
            // _treeView
            // 
            this._treeView.Location = new System.Drawing.Point(15, 57);
            this._treeView.Name = "_treeView";
            this._treeView.Size = new System.Drawing.Size(256, 471);
            this._treeView.TabIndex = 1;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 41);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(68, 13);
            this.label1.TabIndex = 2;
            this.label1.Text = "Specification";
            // 
            // _status
            // 
            this._status.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this._statusMessage});
            this._status.Location = new System.Drawing.Point(0, 539);
            this._status.Name = "_status";
            this._status.Size = new System.Drawing.Size(784, 22);
            this._status.TabIndex = 3;
            this._status.Text = "statusStrip1";
            // 
            // _statusMessage
            // 
            this._statusMessage.Name = "_statusMessage";
            this._statusMessage.Size = new System.Drawing.Size(93, 17);
            this._statusMessage.Text = "Status goes here";
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(784, 561);
            this.Controls.Add(this._status);
            this.Controls.Add(this.label1);
            this.Controls.Add(this._treeView);
            this.Controls.Add(this._menu);
            this.MainMenuStrip = this._menu;
            this.Name = "MainForm";
            this.Text = "HASoftware - Constructor";
            this._menu.ResumeLayout(false);
            this._menu.PerformLayout();
            this._status.ResumeLayout(false);
            this._status.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.MenuStrip _menu;
        private System.Windows.Forms.ToolStripMenuItem _menuFile;
        private System.Windows.Forms.ToolStripMenuItem _menuFileOpen;
        private System.Windows.Forms.ToolStripMenuItem _menuFileExit;
        private System.Windows.Forms.ToolStripMenuItem _menuGenerate;
        private System.Windows.Forms.ToolStripMenuItem _menuGenerateCode;
        private System.Windows.Forms.ToolStripMenuItem _menuGenerateSqlScripts;
        private System.Windows.Forms.ToolStripMenuItem _menuGenerateDocumentation;
        private System.Windows.Forms.ToolStripMenuItem _menuHelp;
        private System.Windows.Forms.ToolStripMenuItem _menuHelpAbout;
        private System.Windows.Forms.TreeView _treeView;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.StatusStrip _status;
        private System.Windows.Forms.ToolStripStatusLabel _statusMessage;
    }
}