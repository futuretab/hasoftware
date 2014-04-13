using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public class OutstandingRequest<T>
    {
        public int TransactionNumber { get; private set; }
        public T Data { get; private set; }

        public OutstandingRequest(int transactionNumber, T data)
        {
            TransactionNumber = transactionNumber;
            Data = data;
        }
    }
}
