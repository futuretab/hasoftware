namespace hasoftware.Util
{
   public interface IController
   {
      string Name();

      bool StartUp();

      bool ReadyToShutDown();

      bool ShutDown();
   }
}
