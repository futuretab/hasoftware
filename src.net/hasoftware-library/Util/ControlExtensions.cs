using System;
using System.Windows.Forms;

namespace hasoftware.Util
{
   public static class ControlExtensions
   {
      public static void InvokeOnUIThread(this Control control, Action action)
      {
         if (control.InvokeRequired)
         {
            MethodInvoker method = () => InvokeOnUIThread(control, action);
            control.BeginInvoke(method);
         }
         else
         {
            action();
         }
      }
   }
}
