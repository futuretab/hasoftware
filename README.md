Home Automation Software
========================

This is where the Home Automation Software will be located. It consists of the following initial modules ...

Manager
-------
A GUI interface into the system with the following initial features
* Fast Page - Send messages to any output device.
* Output Devices - create, edit, update delete output devices.
* Site - Create a heirachy of nodes and input devices.
* Allocations - Allocate input devices to output devices.
* Current Events - View and cancel active input events.

Server
------
Provides TCP/IP remote connections, hides database details, handles user/module logins, notifications and permission checking among other things.

Messaging
---------
The main purpose of this module is to process incoming events and produce outgoing events.

CAN Interface
-------------
Provides access to a custom CAN gateway and bus of devices.

Kirk DECT Phone Interface
-------------------------
Provides access to a the Kirk 500/1500 DECT phone systems allowing messages to be sent to Kirk DECT phones.

SMS Gateway Interface
---------------------
Provides demostration of integration to a SMS gateway, and the Notify My Android service.

Email Interface
---------------
