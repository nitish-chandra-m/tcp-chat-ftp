TCP Socket Programming
---
## Chat Application with FTP capability
Simple TCP chat application with FTP functionality to transfer files between connected peers. 
Peers can connect via TCP sockets and exchange messages and files. 
### Key features:
1. Java Threads are used to manage reading and writing streams concurrently. 
2. Regex matching is used to validate and parse control messages between peers. 
3. Files are sent in 1KB chunks to ensure reliable data transfer over the stream.

_Note: File transfer does not conform fully to FTP standard._

### Usage:
1. Compile the code using javac or any Java compiler.
2. Run the program once with the "java Chat" command. Enter username when prompted, let's say Alice.
3. In another terminal, run the program again with the "java Chat" command. Enter username when prompted, let's say Bob.
4. In Alice's terminal, enter Bob's port number.
5. In Bob's terminal, enter Alice's port number.
6. Enter messages on both terminals to exchange chat messages.
7. Type transfer <filename> on either terminal to send a file to the other party.

