using hasoftware.Cdef;
using hasoftware.Messages;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace hasoftware.Api
{
    public class Message
    {
        private static int NextTransactionNumber;

        public int FunctionCode { get; set; }
        public int TransactionNumber { get; set; }
        public int SystemFlags { get; set; }

        protected Message(int functionCode, int systemFlags)
        {
            FunctionCode = functionCode;
            TransactionNumber = Interlocked.Increment(ref NextTransactionNumber);
            SystemFlags = systemFlags;
        }

        protected Message(int functionCode, int transactionNumber, int systemFlags)
        {
            FunctionCode = functionCode;
            TransactionNumber = transactionNumber;
            SystemFlags = systemFlags;
        }

        protected Message(CdefMessage cdefMessage)
        {
            FunctionCode = cdefMessage.GetU16(0);
            TransactionNumber = cdefMessage.GetU32();
            SystemFlags = cdefMessage.GetU8();
        }

        public virtual void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.Clear();
            cdefMessage.PutU16(FunctionCode);
            cdefMessage.PutU32(TransactionNumber);
            cdefMessage.PutU8(SystemFlags);
        }

        public bool IsRequest
        {
            get { return ((SystemFlags & CdefSystemFlags.Response) == 0); }
        }

        public bool IsResponse
        {
            get { return ((SystemFlags & CdefSystemFlags.Response) == CdefSystemFlags.Response); }
        }

        public bool IsError
        {
            get { return ((SystemFlags & CdefSystemFlags.Error) == CdefSystemFlags.Error); }
        }

        public ErrorResponse CreateErrorResponse()
        {
            return new ErrorResponse(FunctionCode, TransactionNumber);
        }
    }
}
