using hasoftware.Cdef;
using hasoftware.Classes;
using hasoftware.Messages;
using hasoftware.Util;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Api
{
    public class LocalModel : IEventCreator, IEventHandler
    {
        private static readonly LocalModel _instance = new LocalModel();

        public static LocalModel Instance
        {
            get { return _instance; }
        }

        private ConcurrentQueue<Event> _eventQueue;
        private List<OutstandingRequest<int>> _requests;
        private List<CurrentEvent> _currentEvents;
        private List<OutputDevice> _outputDevices;

        private LocalModel()
        {
            _requests = new List<OutstandingRequest<int>>();
            _currentEvents = new List<CurrentEvent>();
            _outputDevices = new List<OutputDevice>();
        }

        public void AddFunctionCodes(List<int> functionCodeList)
        {
            functionCodeList.Add(FunctionCode.OutputDevice);
            functionCodeList.Add(FunctionCode.CurrentEvent);
        }

        public bool SetEventQueue(ConcurrentQueue<Event> eventQueue)
        {
            _eventQueue = eventQueue;
            return true;
        }

        public bool HandleEvent(Event e)
        {
            switch (e.Type)
            {
                case EventType.ReceiveMessage:
                    Message message = e.Message;
                    if (!message.IsError)
                    {
                        if (message.FunctionCode == FunctionCode.Notify)
                        {
                            HandleNotifyResponse((NotifyResponse)message);
                        }
                        else if (message.FunctionCode == FunctionCode.OutputDevice)
                        {
                            HandleOutputDeviceResponse((OutputDeviceResponse)message);
                        }
                        else if (message.FunctionCode == FunctionCode.CurrentEvent)
                        {
                            HandleCurrentEventResponse((CurrentEventResponse)message);
                        }
                    }
                    break;
            }
            return true;
        }

        private void HandleNotifyResponse(NotifyResponse notifyResponse)
        {
            if (notifyResponse.NotifyFunctionCode == FunctionCode.OutputDevice)
            {
                HandleOutputDeviceNotify(notifyResponse.Action, notifyResponse.Ids);
            }
            else if (notifyResponse.NotifyFunctionCode == FunctionCode.CurrentEvent)
            {
                HandleCurrentEventNotify(notifyResponse.Action, notifyResponse.Ids);
            }
        }

        private void HandleCurrentEventNotify(CdefAction action, List<int> ids)
        {
            switch (action)
            {
                case CdefAction.Create:
                case CdefAction.Update:
                    CurrentEventRequest request = new CurrentEventRequest(CdefAction.List);
                    request.Ids.AddRange(ids);
                    _requests.Add(new OutstandingRequest<int>(request.TransactionNumber, 1));
                    _eventQueue.Enqueue(new Event(EventType.SendMessage, request));
                    break;

                case CdefAction.Delete:
                    // TODO RunLater
                    foreach (var id in ids)
                    {
                        foreach (var currentEvent in _currentEvents)
                        {
                            if (currentEvent.Id == id)
                            {
                                _currentEvents.Remove(currentEvent);
                                break;
                            }
                        }
                    }
                    break;
            }
        }

        private void HandleOutputDeviceNotify(CdefAction action, List<int> ids)
        {
            switch (action)
            {
                case CdefAction.Create:
                case CdefAction.Update:
                    OutputDeviceRequest request = new OutputDeviceRequest(CdefAction.List);
                    request.Ids.AddRange(ids);
                    _requests.Add(new OutstandingRequest<int>(request.TransactionNumber, 1));
                    _eventQueue.Enqueue(new Event(EventType.SendMessage, request));
                    break;

                case CdefAction.Delete:
                    // TODO RunLater
                    foreach (var id in ids)
                    {
                        foreach (var outputDevice in _outputDevices)
                        {
                            if (outputDevice.Id == id)
                            {
                                _outputDevices.Remove(outputDevice);
                                break;
                            }
                        }
                    }
                    break;
            }
        }

        private void HandleCurrentEventResponse(CurrentEventResponse currentEventResponse)
        {
            foreach (var request in _requests)
            {
                if (request.TransactionNumber == currentEventResponse.TransactionNumber)
                {
                    if (!currentEventResponse.IsError)
                    {
                        if (currentEventResponse.Action == CdefAction.List)
                        {
                            List<CurrentEvent> currentEventList = currentEventResponse.CurrentEvents;
                            // TODO RunLater
                            foreach (var currentEvent in currentEventList)
                            {
                                // Remove the old item from the list if it already exists
                                foreach (var existingCurrentEvent in _currentEvents)
                                {
                                    if (existingCurrentEvent.Id == currentEvent.Id)
                                    {
                                        _currentEvents.Remove(existingCurrentEvent);
                                        break;
                                    }
                                }
                                // Add the new item to the list
                                _currentEvents.Add(currentEvent);
                            }
                        }
                    }
                }
            }
        }

        private void HandleOutputDeviceResponse(OutputDeviceResponse outputDeviceResponse)
        {
            foreach (var request in _requests)
            {
                if (request.TransactionNumber == outputDeviceResponse.TransactionNumber)
                {
                    if (!outputDeviceResponse.IsError)
                    {
                        if (outputDeviceResponse.Action == CdefAction.List)
                        {
                            List<OutputDevice> outputDeviceList = outputDeviceResponse.OutputDevices;
                            // TODO RunLater
                            foreach (var outputDevice in outputDeviceList)
                            {
                                // Remove the old item from the list if it already exists
                                foreach (var existingOutputDevice in _outputDevices)
                                {
                                    if (existingOutputDevice.Id == outputDevice.Id)
                                    {
                                        _outputDevices.Remove(existingOutputDevice);
                                        break;
                                    }
                                }
                                // Add the new item to the list
                                _outputDevices.Add(outputDevice);
                            }
                        }
                    }
                    _requests.Remove(request);
                    break;
                }
            }
        }
    }
}
