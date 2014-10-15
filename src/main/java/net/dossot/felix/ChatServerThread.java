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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Server thread that takes care of communications with the client.
 * <p>
 * When a client logs in, the thread opens a second communication chanel on a port designated by the
 * client.<br/>
 * This mechanism, where the server thread and the client, are both server/client of a specific
 * communication chanel, allows each peer to receive and send messages at any moment.<br/>
 * In fact the client/server pairs of chanels, are used in one way only, each of them opposed, the
 * other way being reserved for simple acknowledgment messages.
 * <p>
 * 
 * @see net.dossot.felix.ChatServer
 * @see net.dossot.felix.ChatClient
 * @author David Dossot
 * @version 2.0
 */
public class ChatServerThread extends Thread
{
    private ChatServer chatServer = null;
    private Socket socket = null;

    /**
     * Initialize a new chat server thread.
     * 
     * @param ChatServer chatServer A reference of the main server to allow calling methods on it.
     * @param Socket socket The socket that has been open and assigned by the main server.
     */
    public ChatServerThread(final ChatServer chatServer, final Socket socket)
    {
        super("ChatServerThread");
        this.chatServer = chatServer;
        this.socket = socket;
    }

    public void kill()
    {
        try
        {
            socket.close();
        }
        catch (final Exception e)
        {
            // do not report anything, it is a messy murder, after all...
            // System.err.println(e.toString());
        }
    }

    /**
     * The main execution method of the thread, running as long as the communication is established
     * with the client.
     */
    @Override
    public void run()
    {
        if (!chatServer.listening) return;

        ChatMessage message = null;
        String logguedUser = null;

        try
        {
            final ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            while ((chatServer.listening) && (message = (ChatMessage) ois.readObject()) != null)
            {
                logguedUser = message.getSender();

                // manage service messages
                if (message.getService().booleanValue())
                {
                    // manage login
                    if (message.getContents().equals("login"))
                    {
                        synchronized (chatServer)
                        {
                            // parse loggued users and drop any existing user with the same IP
                            // address and port
                            final Collection cnxUsers = chatServer.getLogguedUsers().values();
                            final Iterator iterUsers = cnxUsers.iterator();
                            while (iterUsers.hasNext())
                            {
                                final Vector v = (Vector) iterUsers.next();
                                if ((message.getRecipients().get(0).equals(v.get(0)))
                                    && (message.getRecipients().get(1).equals(v.get(1))))
                                {
                                    System.out.println("Discarded client: " + v.get(0) + ":" + v.get(1));
                                    iterUsers.remove();
                                }
                            }

                            // put the new login info
                            if (null == chatServer.getLogguedUsers()
                                .put(logguedUser, message.getRecipients()))
                                System.out.println("New login: " + logguedUser);
                            else
                                System.out.println("Login re-used: " + logguedUser);
                        }

                        // internal welcome message
                        message = new ChatMessage(new Boolean(true), "host", null, "welcome");
                        oos.writeObject(message);
                        chatServer.dispatchUsers(logguedUser, true);

                        // displayed welcome message
                        final Vector addressee = new Vector();
                        addressee.add(logguedUser);
                        message = new ChatMessage(new Boolean(true), "host", addressee,
                            "Welcome to Felix v" + ChatServer.VERSION + " @ "
                                            + InetAddress.getLocalHost().getHostName());
                        chatServer.sendMessage(logguedUser, message);
                    }

                    // manage logout
                    if (message.getContents().equals("logout"))
                    {
                        final Vector addressee = new Vector();
                        addressee.add(logguedUser);
                        message = new ChatMessage(new Boolean(true), "host", null, "bye");
                        oos.writeObject(message);

                        synchronized (chatServer)
                        {
                            chatServer.getLogguedUsers().remove(logguedUser);
                            chatServer.dispatchUsers(logguedUser, false);
                            System.out.println("Bye to: " + logguedUser);
                        }
                    }
                }

                // manage chat messages
                else
                {
                    // acknowledge
                    final ChatMessage ack = new ChatMessage(new Boolean(true), "host", null, "ack");
                    oos.writeObject(ack);
                    // call the dispatcher
                    chatServer.dispatchMessage(message);
                }
            }

            oos.close();
            ois.close();

        }
        catch (final Exception e)
        {
            // thread terminating : client is gone
        }
    }
}
