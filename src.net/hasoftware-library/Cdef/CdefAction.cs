using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Cdef
{
    public enum CdefAction
    {
        [Description("None")]
        None = 0,
        [Description("Create")]
        Create = 1,
        [Description("Delete")]
        Delete = 2,
        [Description("List")]
        List = 3,
        [Description("Update")]
        Update = 4
    }
}
