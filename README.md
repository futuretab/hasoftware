All the components are wriiten in Java as I need the main backend to run on Linux and the manager to run on Windows.
The communication protocols are simple enough that components could just as eailty be written in any other language of choice ... In fact I have added a .NET port of the library and the beginnings of a sample .NET application.

It consists of the following initial modules ... all work in progress.

Manager (Java)
--------------
A GUI interface into the system with the following initial features
* Fast Page - Send messages to any output device.
* Output Devices - create, edit, update delete output devices.
* Site - Create a heirachy of nodes and input devices.
* Allocations - Allocate input devices to output devices.
* Current Events - View and cancel active input events.

Server (Java)
-------------
Provides TCP/IP remote connections, hides database details, handles user/module logins, notifications and permission checking among other things.

Messaging (Java)
----------------
The main purpose of this module is to process InputMessages and create OutputMessage using the configured Allocations.
It will also maintain the ActiveEvent and HistoricalEvent information.

CAN Interface (Java)
--------------------
Provides access to a custom CAN gateway and bus of devices.

Kirk DECT Phone Interface (Java)
--------------------------------
Provides access to a the Kirk 500/1500 DECT phone systems allowing messages to be sent to Kirk DECT phones.

SMS Gateway Interface (Java)
----------------------------
Provides demostration of integration to
* an SMS Web Service
* Notify My Android
* F1103 GPRS Modem

Email Interface (Java)
----------------------
TODO

Current Events Screen (.NET)
----------------------------
Allows a customizable view of what it going on in the system.
