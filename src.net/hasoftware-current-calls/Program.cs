using hasoftware_current_calls.Forms;
using NLog;
using System;
using System.Windows.Forms;

namespace hasoftware_current_calls
{
    static class Program
    {
        private static Logger logger = LogManager.GetCurrentClassLogger();

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            logger.Debug("--------------------------------------------------------------------------------");
            logger.Debug("{0} {1} {2}", Application.CompanyName, Application.ProductName, Application.ProductVersion);
            logger.Debug("--------------------------------------------------------------------------------");
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new CurrentCallsForm());
        }
    }
}
