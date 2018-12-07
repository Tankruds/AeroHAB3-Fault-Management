# AeroHAB3-Fault-Management
This is a program meant to find errors with data collection during the flight of AeroHAB 3.
Error is data type that stores the time, location, and a basic message about the error.
ErrorDB is a data type that has an ArrayList that holds Errors for us.
FaultManagement is the bulk of the code and has the main method; it uses a socket to communicate with the Sensor Group (Seperate code)
We then get the data from them and do some simple error checking, we then log any errors we see in ErrorDB and
store it in a file to look at later. If you pass in parameters, the first being the amount of time we wait to pass before disconnecting (in seconds)
the second being how high in altitude you would like to go before disconnecting.
We disconnect using a Nichrome disconnect method where we set pin 17 to high while connected to a relay which will heat up some Nichrome and cut the cable loose.
