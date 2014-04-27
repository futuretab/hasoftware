using System;
using hasoftware.Cdef;
using hasoftware.Messages;
using System.Threading;

namespace hasoftware.Api
{
    public class Message
    {
        private static int NextTransactionNumber;

        public int FunctionCode { get; set; }
        public int TransactionNumber { get; set; }
        public int SystemFlags { get; set; }

        protected Message()
        {
            FunctionCode = CdefFunctionCode.None;
            TransactionNumber = Interlocked.Increment(ref NextTransactionNumber);
            SystemFlags = 0;
        }

        protected Message(int functionCode, int transactionNumber, int systemFlags)
        {
            FunctionCode = functionCode;
            TransactionNumber = transactionNumber;
            SystemFlags = systemFlags;
        }

        protected Message(CdefMessage cdefMessage)
        {
            FunctionCode = cdefMessage.GetInt(0);
            TransactionNumber = cdefMessage.GetInt();
            SystemFlags = cdefMessage.GetInt();
        }

        protected void Decode(CdefMessage cdefMessage)
        {
            FunctionCode = cdefMessage.GetInt(0);
            TransactionNumber = cdefMessage.GetInt();
            SystemFlags = cdefMessage.GetInt();
        }

        public virtual void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.Clear();
            cdefMessage.PutInt(FunctionCode);
            cdefMessage.PutInt(TransactionNumber);
            cdefMessage.PutInt(SystemFlags);
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
            return new ErrorResponse(TransactionNumber) { FunctionCode = FunctionCode };
        }
    }
}
