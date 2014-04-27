using hasoftware.model;

namespace hasoftware.handlers
{
    public interface IHandler
    {
        void Handle(Specification specification);
    }
}
