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

import java.util.Vector;

/**
 * This is a dummy GUI for creating non visible chat clients <i>It is used to send a shutdown
 * message to the server</i>
 * 
 * @author David Dossot
 * @version 2.0
 */

public class ChatUIImpl implements net.dossot.felix.ChatUI
{
    private boolean chatEnabled = false;
    private Vector usersList = null;
    private ChatMessage lastMessage = null;

    /** Turn on or off the gui features for typing/sending messages. */
    @Override
    public void setChatEnabled(final boolean enabled)
    {
        chatEnabled = enabled;
    }

    /** Display the list of currently loggued users listed in the vector of strings <i>fullList</i>. */
    @Override
    public void ShowListUsers(final Vector fullList)
    {
        usersList = fullList;
    }

    /** Display a chat message. */
    @Override
    public void ShowMessage(final ChatMessage message)
    {
        lastMessage = message;
    }

    /**
     * Getter for property chatEnabled.
     * 
     * @return Value of property chatEnabled.
     */
    public boolean isChatEnabled()
    {
        return chatEnabled;
    }

    /**
     * Getter for property lastMessage.
     * 
     * @return Value of property lastMessage.
     */
    public net.dossot.felix.ChatMessage getLastMessage()
    {
        return lastMessage;
    }

    /**
     * Getter for property usersList.
     * 
     * @return Value of property usersList.
     */
    public java.util.Vector getUsersList()
    {
        return usersList;
    }

}
