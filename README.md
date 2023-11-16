# Java Chat App
## This is a simple application that allows to create a chat server and connect multiple clients to it.

## To run it:
* Launch the server by calling: java Server.java
* Launch clients by calling: java Client.java

## Functionality
Once a user has joined, they have to provide their username for the conversation.
```
Connected
Enter you username:
```
Afterwards, the entire conversation since the start of the server will be loaded for the user.
Now they can send messages by typing them into the console and pressing Enter.
To exit conversation, the user must close the console.

The Server.java file will create a folder called "logs" where it will store transcripts of all current and past chat conversations.