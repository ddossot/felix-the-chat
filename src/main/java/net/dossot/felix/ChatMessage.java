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

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

/**
 * This class holds any message exchanged in Felix. It implements <i>Serializable</i> in order to be
 * exchanged in <i>Object Streams</i>. The attributes are public accessed, no getter/setter have
 * been implemented to keep this class as simple as possible.
 * 
 * @author David Dossot
 * @version 2.0
 */

public class ChatMessage implements Serializable
{
    /** Flag saying if it is a service (internal) message. */
    private Boolean service;
    /** User name of the sender of the message. */
    private String sender;
    /**
     * Vector of strings containing the user names of the recipients of the message. An empty vector
     * means the message is sent to all loggued users.
     * <p>
     * For service messages, the vector is used to hold different kind of information.
     * </p>
     */
    private Vector recipients;
    /** Date/Time when the message has been created. */
    private Date dateSent;
    /** Textual content of the message. */
    private String contents;

    /**
     * Creates a new ChatMessage
     * 
     * @param Boolean Service
     * @param String Sender
     * @param Vector Recipients
     * @param String Contents
     */
    public ChatMessage(final Boolean Service,
                       final String Sender,
                       final Vector Recipients,
                       final String Contents)
    {
        service = Service;
        sender = Sender;
        recipients = Recipients;
        dateSent = new Date();
        contents = Contents;
    }

    /**
     * Returns a string representation of the message.
     * 
     * @return String representation of the message.
     */
    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer("ChatMessage: ");
        sb.append("Service=");
        sb.append(service.booleanValue());
        sb.append(", Sender=");
        sb.append(sender);
        sb.append(", Recipients=");
        sb.append(recipients);
        sb.append(", Date=");
        sb.append(dateSent.toString());
        sb.append(", Contents=");
        sb.append(contents);
        return sb.toString();
    }

    /**
     * Getter for property contents.
     * 
     * @return Value of property contents.
     */
    public java.lang.String getContents()
    {
        return contents;
    }

    /**
     * Setter for property contents.
     * 
     * @param contents New value of property contents.
     */
    public void setContents(final java.lang.String contents)
    {
        this.contents = contents;
    }

    /**
     * Getter for property dateSent.
     * 
     * @return Value of property dateSent.
     */
    public java.util.Date getDateSent()
    {
        return dateSent;
    }

    /**
     * Setter for property dateSent.
     * 
     * @param dateSent New value of property dateSent.
     */
    public void setDateSent(final java.util.Date dateSent)
    {
        this.dateSent = dateSent;
    }

    /**
     * Getter for property recipients.
     * 
     * @return Value of property recipients.
     */
    public java.util.Vector getRecipients()
    {
        return recipients;
    }

    /**
     * Setter for property recipients.
     * 
     * @param recipients New value of property recipients.
     */
    public void setRecipients(final java.util.Vector recipients)
    {
        this.recipients = recipients;
    }

    /**
     * Getter for property sender.
     * 
     * @return Value of property sender.
     */
    public java.lang.String getSender()
    {
        return sender;
    }

    /**
     * Setter for property sender.
     * 
     * @param sender New value of property sender.
     */
    public void setSender(final java.lang.String sender)
    {
        this.sender = sender;
    }

    /**
     * Getter for property service.
     * 
     * @return Value of property service.
     */
    public java.lang.Boolean getService()
    {
        return service;
    }

    /**
     * Setter for property service.
     * 
     * @param service New value of property service.
     */
    public void setService(final java.lang.Boolean service)
    {
        this.service = service;
    }

}
