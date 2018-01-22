using System.Collections.Concurrent;

namespace hasoftware.Util
{
   public abstract class AbstractController : IController, IEventCreator, IEventHandler
   {
      public abstract string Name();
      public abstract bool StartUp();
      public abstract bool ReadyToShutDown();
      public abstract bool ShutDown();
      public abstract bool SetEventQueue(ConcurrentQueue<Event> eventQueue);
      public abstract bool HandleEvent(Event e);
   }
}
