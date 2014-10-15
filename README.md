# Felix the Chat

> This is a super old project (2001-2002), shared on GitHub because it's apparently in use (in 2014!) and its source code was needed.

Felix is an open source chat program running on J2SE. It has a very simple design, with only a few classes and a plugable GUI that you can replace with your own. It is designed to be integrated in your own application to serve a few number of users, typically in a corporate intranet.

It is multi-threaded and socket based (using two ports). Messages are simple objects that are transmitted thanks to Java's serialization mechanism. 

Felix is working with a plugable GUI: it comes with a sample UI where chatting is done in a unique board. You can create and plug your own UI (for example, in a JDesktopPane open one JInternalFrame per individual chat thread) and for this, you just need to implement the interface ChatUI.

## Running

Starting the server: `java -cp felix-2.0.2.jar net.dossot.felix.ChatServer {$port} {-shutdown}`

> The optional parameter `-shutdown` sends an internal shutdown request to the server designated by its `$host`.

Starting the GUI client (JDK1.2): `java -jar FelixTheChat.jar {$userName} {$hostNameOrIP} {$port} {-noAppletSounds}`

Starting the GUI client (JDK1.1): `java -cp FelixTheChat.jar net.dossot.felix.ui.Client {$userName} {$hostNameOrIP} {$port} {-noAppletSounds}`

> The optional parameter `-noAppletSounds` forces the proposed UI client to use system beeps for incoming messages (recommended on NT and on any platform where applet sounds steal the sound resources).

## Known problems of the current version:

    On NT4, the sound resources are stolen by Java, therefore not available for the other applications. Possible cure: write your own sound access DLL in JNI, or start the client with -noAppletSounds option.
    The list of users sometimes displays a long list of CRLF instead of one user name (serialization problem ?)

NB. these problems seem more UI related and not related to the base chat classes. Bear in mind that the UI class is a proposed basic one: you are kindly invited to create your own one!

## Version history:

<pre>
[1.1]   First official release                                    (03-nov-2001)
[1.2]   ui.Client : Improvement of chat board refreshment         (07-nov-2001)
        ChatServer: {$hostPort} command line argument added
        Chatclient: Host port parameter added on the constructor
[1.2.1] (not released)
[1.2.2] ChatServer: Server shutdown implemented                   (17-Dec-2001)
        ui.Client : No applet sound in Swing GUI implemented
[1.2.3] ui.Client : Chat board reset button implemented           (04-feb-2002)
                    Improvement of chat board refreshment (again...)
[2.0.2] ChatServer/ChatClient:                                    (03-nov-2001)
                    Fully disconnected, ie sockets are opened/closed on each call, reducing the impact of network problems.
                    NB. You can not use a previous client with this new version of the server.
        ui.Client : Now supports custom emoticons
</pre>
