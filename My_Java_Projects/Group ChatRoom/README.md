# Overview
## Introduction

This is a command-line-based group chatting room. It's built directly on the TCP layer to send and receive messages.

The chatting room consists of a single server and a limited number of clients. The server runs the main thread for accepting new clients, and will start a new thread every time a new client has joined the chatting room, for receiving messages from the new client and forwarding the messages to other clients. The client runs two thread, one for taking input from the keyboard, the other for receiving messages from the server. 

## Message Wrapping

Messages between the server and the clients are wrapped in a JSON format. The field names are:

1. type: The type of the message
2. username: The name of the client that initiates the message in the packet
3. content: The actual message in the packet

There are seven types of packets:

For clients:

1. JOIN: After a TCP connection has been set up, a client sends a JOIN packet with its client name to the server.
2. SEND: As long as there is a valid user input from the keyboard, the input is wrapped in a SEND packet and sent to the server.

For the server:

1. FWD: After a message is received from a client, the server wraps the message in a FWD packet and sends it to all the other clients.
2. NAK: If the number of clients is over the limit, or the new client has a user name that already exists, then the server sends an NAK packet to reject the new client.
3. OFFLINE: When a user drops, the server notifies others of the client's departure with OFFLINE packets.
4. ACK: The server responds to a JOIN packet with an ACK packet to notify the new client of its acceptance.
5. ONLINE: When a user joins the chatting room, the server notifies others of the new client's joining.
 
# User Guidance

## Server Code

The server should start before any client starts. It should be started from command lines by:

javac -cp path_to_the_folder_of_gson_jar_files;path_to_gson_jar_files Main.java
java -cp path_to_the_folder_of_gson_jar_files;path_to_gson_jar_files Main TCP_port_number max_client_number

The client should be started from command lines by:

javac -cp path_to_the_folder_of_gson_jar_files;path_to_gson_jar_files Main.java
java -cp path_to_the_folder_of_gson_jar_files;path_to_gson_jar_files Main server_IP TCP_port_number user_name

For example, if the gson jar files are in D:\server, then the commands to start the server are:

javac -cp D:\server;D:\server\gson-2.6.1.jar Main.java
java -cp D:\server;D:\server\gson-2.6.1.jar Main 50000 20

Remember, since we don't know about the threading security of gson jar files, it's recommanded to use a distinct copy of gson jar file for every client and server when you test the code on a single computer.
