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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * The main server of Felix (console mode). It opens a socket, waits for connections from the
 * clients and spawns a thread for each of them.
 * 
 * @see net.dossot.felix.ChatServerThread
 * @author David Dossot
 * @version 2.0.2
 */
public class ChatServer
{
    public final static String VERSION = "2.0.2";
    protected boolean listening = true;
    private final Object dispatcher = new Object();
    private Timer userListDaemon = null;

    /** Hashtable containing references to the different clients. */
    private volatile Hashtable logguedUsers = new Hashtable();
    private volatile String[] previousLogguedUsers = null;

    /**
     * Send a message to a client.
     * 
     * @param String recipient The recipient of the message.
     * @param ChatMessage message The message to be sent.
     * @see net.dossot.felix.ChatServer#dispatchMessage(ChatMessage)
     */
    protected synchronized boolean sendMessage(final String recipient, ChatMessage message)
    {
        boolean result = false;
        final Vector clientTCPInfo = (Vector) getLogguedUsers().get(recipient);

        if (clientTCPInfo != null)
        {
            final String clientIP = (String) clientTCPInfo.get(0);
            final Integer clientPort = (Integer) clientTCPInfo.get(1);

            try
            {
                final Socket csSocket = new Socket(clientIP, clientPort.intValue());
                csSocket.setSoTimeout(10000);
                final ObjectOutputStream soos = new ObjectOutputStream(csSocket.getOutputStream());
                final ObjectInputStream sois = new ObjectInputStream(csSocket.getInputStream());
                soos.writeObject(message);
                if ((message = (ChatMessage) sois.readObject()) != null)
                    if ((message.getService().booleanValue()) && (message.getContents().equals("ack")))
                        result = true;

                soos.close();
                sois.close();
                csSocket.close();
            }
            catch (final Exception e)
            {
                // something went wrong, sniping will occur
            }

            if (!result)
            {
                // snipe the user
                System.out.println("Sniped user: " + recipient);
                getLogguedUsers().remove(recipient);
                scheduleUserListRefresh();
            }
        }
        else
        {
            // unknown user: can not route message
            System.out.println("Can not route message to: " + recipient);
        }

        return result;
    }

    /**
     * Send the list of users to the client of this thread. The users list request is a service
     * message that contains a vector holding the names of all the loggued users.
     * 
     * @param String logguedUser Name of the user leaving or joining the chat, originating the
     *            refreshment of all lists of loggued users.
     * @param boolean coming True if the user is joining, false if he is leaving.
     * @see net.dossot.felix.ChatServer#dispatchUsers(String, boolean)
     */
    protected synchronized void sendUsers(final String destination, final String newUser, final boolean coming)
    {
        final StringBuffer contents = new StringBuffer("{$USERS}");
        if (newUser != null)
        {
            contents.append(newUser);
            contents.append(" has ");
            contents.append((coming ? "joined" : "left"));
            contents.append(" the chat.");
        }
        else
            contents.append("Ghost users have vanished...");

        final ChatMessage message = new ChatMessage(new Boolean(true), "host", new Vector(
            getLogguedUsers().keySet()), contents.toString());
        sendMessage(destination, message);
    }

    /**
     * Invoke sendUsers for all loggued users.
     * <p>
     * <b>dispatchUsers is mutually exclusive (synchronized) with dispatchMessage</b>
     * </p>
     * 
     * @param String logguedUser Name of the user leaving or joining the chat, originating the
     *            refreshment of all lists of loggued users.
     * @param boolean coming True if the user is joining, false if he is leaving.
     * @see net.dossot.felix.ChatServer#sendUsers(String, String, boolean)
     */
    protected synchronized void dispatchUsers(final String logguedUser, final boolean coming)
    {
        final ChatServerThread recipient = null;
        synchronized (dispatcher)
        {
            setPreviousLogguedUsers((String[]) getLogguedUsers().keySet().toArray(new String[1]));

            for (final Enumeration e = getLogguedUsers().keys(); e.hasMoreElements();)
                sendUsers((String) e.nextElement(), logguedUser, coming);
        }
    }

    /**
     * Invoke sendMessage in all client threads concerned by the message. This method takes care of
     * the messaging feedback to the sender, i.e. if the sender is not in the recipient list,
     * sendMessages takes care of sending the message to him.
     * <p>
     * <b>dispatchMessage is mutually exclusive (synchronized) with dispatchUsers</b>
     * </p>
     * 
     * @param ChatMessage message The message to be sent.
     * @see net.dossot.felix.ChatServer#sendMessage(String, ChatMessage)
     */
    protected synchronized void dispatchMessage(final ChatMessage message)
    {
        boolean shutdown = false;
        boolean oneSent = false;
        String recipient = null;

        synchronized (dispatcher)
        {
            if (getLogguedUsers().containsKey(message.getSender()))
            {
                // check internal admin messages
                if (message.getRecipients().size() == 1)
                {
                    recipient = (String) message.getRecipients().get(0);
                    if ((recipient.equals("host")) && (message.getSender().equals("host::admin")))
                    {
                        if (message.getContents().equals("shutdown::immediate"))
                        {
                            shutdown = true;
                            // alter message nature to cast it to everyone
                            // this message is understood by the client as a invitation to log off
                            message.setSender("host");
                            message.setContents("{$SHUTDOWN}Server shutdown initiated by the administrator. You will be loggued off!");
                            message.setRecipients(new Vector());
                            message.setService(new Boolean(true));
                        }
                    }
                }

                if (message.getRecipients().size() == 0)
                {
                    // it is a general message
                    for (final Enumeration e = getLogguedUsers().keys(); e.hasMoreElements();)
                    {
                        recipient = (String) e.nextElement();
                        if (!recipient.equals(message.getSender()))
                            if (sendMessage(recipient, message)) oneSent = true;
                    }
                }
                else
                {
                    // it is a targeted message
                    for (final Enumeration e = message.getRecipients().elements(); e.hasMoreElements();)
                    {
                        recipient = (String) e.nextElement();
                        if ((getLogguedUsers().get(recipient) != null)
                            && (!recipient.equals(message.getSender())))
                            if (sendMessage(recipient, message)) oneSent = true;
                    }
                }

                // if at least one message reached its recipient, send the message to him as a
                // feedback
                if (oneSent)
                {
                    sendMessage(message.getSender(), message);
                }
                else
                {
                    // alter message nature to show an error
                    final String keepRecipient = message.getSender();
                    message.setSender("host");
                    message.setContents("Your message has been lost (in space).");
                    message.setService(new Boolean(true));
                    sendMessage(keepRecipient, message);
                }
            }
            else
            {
                // unknown user: rejected message
                System.out.println("Rejected message from: " + message.getSender());
            }
        }

        if (shutdown)
        {
            System.out.println("\n>>> Shuting down.");
            listening = false;
            System.out.println(">>> Done. Exiting JVM. Have a nice day ;-)");
            System.exit(0);
        }
    }

    /** (to comment) */
    private void scheduleUserListRefresh()
    {
        try
        {
            if (userListDaemon == null) userListDaemon = new Timer(true);

            userListDaemon.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    if ((getPreviousLogguedUsers() != null)
                        && (!Arrays.equals(getPreviousLogguedUsers(),
                            getLogguedUsers().keySet().toArray(new String[1]))))
                    {
                        dispatchUsers(null, false);
                        System.out.println("Daemon has refreshed the user list.");
                    }
                }
            }, 0, 20000);
        }
        catch (final Exception e)
        {
            System.err.println("Daemon can schedule the user list refresher.");
            e.printStackTrace();
        }
    }

    /** (to comment) */
    private ChatServer(final int port) throws IOException
    {
        ServerSocket serverSocket = null;

        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch (final IOException e)
        {
            System.err.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        System.out.println("****************************************\n    FELIX " + VERSION
                           + " - Chat Server Running\n    Address: " + InetAddress.getLocalHost()
                           + "\n       Port: " + port + "\n****************************************");

        scheduleUserListRefresh();
        while (listening)
            new ChatServerThread(this, serverSocket.accept()).start();

        serverSocket.close();
    }

    /**
     * Start or stop the chat server. Required argument : server port number Optional argument :
     * -shutdown The console will display the server name and IP address you can communicate to the
     * chat users.
     */
    public static void main(final String[] args) throws Exception
    {
        final int portNumber = Integer.parseInt(args[0]);

        if ((args.length == 2) && (args[1].equals("-shutdown")))
        {
            final ChatUIImpl client = new ChatUIImpl();
            // connects to the server to stop it
            final ChatClient chatClient = new ChatClient("host::admin", "localhost", portNumber, client);
            if (chatClient.requestLogin())
            {
                final Vector host = new Vector();
                host.add("host");
                chatClient.sendMessage(false, host, "shutdown::immediate", "???");
            }
            else
                System.err.println("Impossible to connect to the chat server for stopping it.");
        }
        else
        {
            // starting a new server
            final ChatServer cs = new ChatServer(portNumber);
        }
    }

    /**
     * Getter for property logguedUsers.
     * 
     * @return Value of property logguedUsers.
     */
    public java.util.Hashtable getLogguedUsers()
    {
        return logguedUsers;
    }

    /**
     * Setter for property logguedUsers.
     * 
     * @param logguedUsers New value of property logguedUsers.
     */
    public void setLogguedUsers(final java.util.Hashtable logguedUsers)
    {
        this.logguedUsers = logguedUsers;
    }

    /**
     * Getter for property previousLogguedUsers.
     * 
     * @return Value of property previousLogguedUsers.
     */
    public String[] getPreviousLogguedUsers()
    {
        return previousLogguedUsers;
    }

    /**
     * Setter for property previousLogguedUsers.
     * 
     * @param previousLogguedUsers New value of property previousLogguedUsers.
     */
    public void setPreviousLogguedUsers(final String[] previousLogguedUsers)
    {
        this.previousLogguedUsers = previousLogguedUsers;
    }

}
