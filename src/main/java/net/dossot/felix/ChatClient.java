//
//    Felix the Chat
//
//    Copyright (c) 2001-2002 David Dossot
//
//    Permission is hereby granted, free of charge, to any person obtaining a copy
//    of this software and associated documentation files (the "Software"), to deal
//    in the Software without restriction, including without limitation the rights
//    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//    copies of the Software, and to permit persons to whom the Software is
//    furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
//    all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//    THE SOFTWARE.
//

package net.dossot.felix;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Main chat client class.
 * <p>
 * that takes care of contacting the server and creates a thread to listen for the messages it could
 * send.
 * </p>
 * <p>
 * It is usually instantiated like this:<br/>
 * <code>chatClient = new ChatClient(userName, hostName, thisGUI);<br/>
 * if (chatClient.requestLogin()) { ... }</code>
 * </p>
 * <p>
 * It is usually terminated like this:<br/>
 * <code>if (chatClient.requestLogout()) { ... }<br/>
 * chatClient = null;</code>
 * </p>
 * 
 * @see net.dossot.felix.ChatServerThread
 * @see net.dossot.felix.ChatServer
 * @author David Dossot
 * @version 2.0
 */
public class ChatClient
{
    private String userName = null;
    private String chatServerHost = null;
    private final int chatServerPort;

    private Vector logguedUsers = null;
    private boolean loggued = false;
    private ServerSocket serverSocket = null;
    private String chatClientHost = null;

    private ChatUI uiClient = null;
    private Thread chatClientServerThread = null;
    private int port;

    /**
     * Initialize a chat client session. After connecting to the server socket, the client tries to
     * be a server itself on the base port + 1. If this port is not available, it will increment ten
     * times to try to find a free port. It means that you can have up to ten clients running on the
     * same machine.
     * 
     * @param String iUserName Name of the user for the chat session.
     * @param String iChatServerHost Name or IP address of the chat server.
     * @param int iPortNumber Host port number.
     * @param ChatUI iuiClient GUI to be used by this client instance.
     */
    public ChatClient(final String iUserName,
                      final String iChatServerHost,
                      final int iPortNumber,
                      final ChatUI iuiClient)
    {
        userName = iUserName;
        chatServerHost = iChatServerHost;
        chatServerPort = iPortNumber;
        uiClient = iuiClient;
        port = iPortNumber;
        try
        {
            chatClientHost = InetAddress.getLocalHost().getHostAddress();
        }
        catch (final Exception e)
        {
            reportException("Can't find local address", e);
        }

        final ChatMessage message = null;
        while (true)
        {
            try
            {
                port++;
                serverSocket = new ServerSocket(port);
                break;
            }
            catch (final IOException e)
            {
                if (port > (iPortNumber + 10)) reportException("Couldn't find any available port", e);
            }
        }

        // start a thread listening to incoming server messages
        try
        {
            chatClientServerThread = new ChatClientServerThread();
            chatClientServerThread.start();
        }
        catch (final Exception e)
        {
            reportException("Can't start client service message server", e);
        }
    }

    /** User name made accessible for the GUI. */
    public String getUserName()
    {
        return (userName);
    }

    /**
     * Send a text message (not a service message).
     * 
     * @param Vector recipients Vector of strings containing the user names of the recipients of the
     *            message. An empty vector means the message is sent to all loggued users.
     * @param String contents Textual content of the message.
     */
    public boolean sendMessage(final boolean adminMessage,
                               final Vector recipients,
                               final String contents,
                               final String expectedAnswer)
    {
        boolean result = false;
        try
        {
            final Socket csSocket = new Socket(chatServerHost, chatServerPort);
            csSocket.setSoTimeout(10000);
            final ObjectOutputStream oos = new ObjectOutputStream(csSocket.getOutputStream());
            final ObjectInputStream ois = new ObjectInputStream(csSocket.getInputStream());
            ChatMessage message = new ChatMessage(new Boolean(adminMessage), userName, recipients, contents);
            oos.writeObject(message);

            if ((message = (ChatMessage) ois.readObject()) != null)
                if ((message.getService().booleanValue()) && (message.getContents().equals(expectedAnswer)))
                    result = true;

            oos.close();
            ois.close();
            csSocket.close();
        }
        catch (final Exception e)
        {
            result = false;
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Login to the chat server. The login request is a service message that contains a vector
     * holding information about the IP address and port open on the client machine, for the second
     * chanel of communication.
     */
    public boolean requestLogin()
    {
        boolean result = false;
        final ChatMessage message = null;
        loggued = false;
        final Vector v = new Vector();
        v.add(chatClientHost);
        v.add(new Integer(port));
        if (sendMessage(true, v, "login", "welcome"))
        {
            loggued = true;
            result = true;
        }
        return result;
    }

    /** Logout of the chat server. */
    public boolean requestLogout()
    {
        boolean result = false;
        final ChatMessage message = new ChatMessage(new Boolean(true), "client", null, "Requesting logout...");
        uiClient.ShowMessage(message);

        if (sendMessage(true, null, "logout", "bye"))
        {
            result = true;
            loggued = false;
            logguedUsers = null;
            chatClientServerThread.destroy();
        }
        return result;
    }

    private void displayLogguedUsers()
    {
        uiClient.ShowListUsers(logguedUsers);
    }

    private void reportException(final String title, final Exception e)
    {
        final ChatMessage message = new ChatMessage(new Boolean(true), "client", null, title + ": "
                                                                                       + e.getMessage());
        uiClient.ShowMessage(message);
    }

    /*
     * ----------------------------------------------------------------------------------------------
     * --------------------
     */

    class ChatClientServerThread extends Thread
    {
        private boolean stopIt = false;

        public ChatClientServerThread()
        {
            super("ChatClientServerThread");
            setDaemon(true);
        }

        @Override
        public void destroy()
        {
            stopIt = true;
        }

        @Override
        public void run()
        {
            try
            {
                while (!stopIt)
                    new ChatClientThread(serverSocket.accept()).start();
            }
            catch (final Exception e)
            {
                reportException("Can't start client service message server", e);
            }
        }
    }

    /*
     * ----------------------------------------------------------------------------------------------
     * --------------------
     */

    class ChatClientThread extends Thread
    {
        // open second chanel messages object streams
        private final Socket socket;
        private ObjectOutputStream soos;
        private ObjectInputStream sois;
        private boolean stopIt = false;

        public ChatClientThread(final Socket socket)
        {
            super("ChatClientThread");
            setDaemon(true);
            this.socket = socket;
        }

        @Override
        public void destroy()
        {
            stopIt = true;
        }

        @Override
        public void run()
        {
            ChatMessage message = null;
            try
            {
                soos = new ObjectOutputStream(socket.getOutputStream());
                sois = new ObjectInputStream(socket.getInputStream());
                while ((!stopIt) && (message = (ChatMessage) sois.readObject()) != null)
                {
                    // acknowledge
                    final ChatMessage ack = new ChatMessage(new Boolean(true), userName, null, "ack");
                    soos.writeObject(ack);
                    // manage service messages
                    if (message.getService().booleanValue())
                    {
                        // manage refresh list of users
                        if (message.getContents().startsWith("{$USERS}"))
                        {
                            logguedUsers = message.getRecipients();
                            displayLogguedUsers();
                            message.setContents(message.getContents().substring(8));
                            uiClient.ShowMessage(message);
                        }
                        // manage server shutdown message
                        else if (message.getContents().startsWith("{$SHUTDOWN}"))
                        {
                            message.setContents(message.getContents().substring(11));
                            uiClient.ShowMessage(message);
                            uiClient.setChatEnabled(false);
                            stopIt = true;
                            break;
                        }
                        // displayable system message
                        else
                        {
                            uiClient.ShowMessage(message);
                        }
                    }
                    // manage chat messages
                    else
                    {
                        uiClient.ShowMessage(message);
                    }
                }

                soos.close();
                sois.close();
                socket.close();
            }
            catch (final Exception e)
            {
                // termination on the server side
            }
        }
    }
}
