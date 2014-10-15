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

package net.dossot.felix.ui;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Image;
import java.text.DateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.dossot.felix.ChatClient;
import net.dossot.felix.ChatMessage;
import net.dossot.felix.ChatUI;

/**
 * This is a sample GUI where all chatting is done in a single board and message privacy is shown
 * with a range of colors and sounds. <i>It is not documented on purpose as it is not part of
 * Felix's core</i>
 * 
 * @author David Dossot
 * @version 2.3
 */

public class Client extends javax.swing.JFrame implements ChatUI
{
    private static final String VERSION = "Felix Standard UI v2.3";

    private static Client guiClient = null;
    private ChatClient chatClient = null;
    private StringBuffer chatBoard = new StringBuffer();
    private final DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    private boolean toggleSelection = true;
    private boolean currentTitleState = true;

    // sounds
    private static boolean appletSounds = true;
    private final Hashtable sounds = new Hashtable();
    private final String soundList[] = {"alarm", "all", "group", "joined", "left", "private", "sent"};
    private final int beepList[] = {3, 1, 2, 0, 0, 2, 0};

    // icons
    private ResourceBundle emoticons = null;
    private Image iconFelix = null;
    private Image iconMessage = null;

    private final Runnable updateChatBoardDisplay = new Runnable()
    {
        @Override
        public void run()
        {
            editMessages.setText("<font face='arial,helvetica'>" + chatBoard + "</font>");
            javax.swing.RepaintManager.currentManager(guiClient).paintDirtyRegions();
        }
    };

    public Client(final String userName, final String hostName, final int hostPort)
    {
        try
        {
            emoticons = ResourceBundle.getBundle("emoticons", Locale.getDefault());
        }
        catch (final Exception e)
        {
            emoticons = null;
        }

        initComponents();
        loadIcons();
        setTitleBar(false);
        pack();

        initSounds();
        chatClient = new ChatClient(userName, hostName, hostPort, this);
        setChatEnabled(chatClient.requestLogin());
    }

    @Override
    public void setChatEnabled(final boolean enabled)
    {
        txtSend.setEnabled(enabled);
        btnSend.setEnabled(enabled);
        if (enabled)
        {
            txtSend.requestFocus();
        }
        else
        {
            ShowListUsers(new Vector());
            listUsers.repaint();
            playSound("alarm");
        }
        lblUserName.setText(enabled ? chatClient.getUserName() : "Not loggued");
        lblUserName.repaint();
        repaint();
    }

    private void setTitleBar(final boolean hasMessage)
    {
        if (currentTitleState != hasMessage)
        {
            currentTitleState = hasMessage;
            setTitle((hasMessage ? "* MESSAGE * " : "") + VERSION);
            setIconImage(hasMessage ? iconMessage : iconFelix);
        }
    }

    private void loadIcons()
    {
        // load emoticon buttons
        if (emoticons != null)
        {
            final JPanel leftPanel = new JPanel();
            final BoxLayout bl = new BoxLayout(leftPanel, BoxLayout.Y_AXIS);
            leftPanel.setLayout(bl);
            String emoticonKey;
            String emoticonImg;
            final Vector loadedImg = new Vector();
            JButton emoticonButton;
            final Enumeration emoticonKeys = emoticons.getKeys();
            while (emoticonKeys.hasMoreElements())
            {
                emoticonKey = (String) emoticonKeys.nextElement();
                emoticonImg = emoticons.getString(emoticonKey);
                if (!loadedImg.contains(emoticonImg))
                {
                    emoticonButton = new JButton("<html><img src='" + emoticonImg + "'></html>");
                    emoticonButton.setActionCommand(emoticonKey);
                    emoticonButton.setBackground(java.awt.Color.white);
                    emoticonButton.addActionListener(new java.awt.event.ActionListener()
                    {
                        @Override
                        public void actionPerformed(final java.awt.event.ActionEvent evt)
                        {
                            setTitleBar(false);
                            txtSend.setText(txtSend.getText() + " " + evt.getActionCommand() + " ");
                            txtSend.requestFocus();
                        }
                    });
                    leftPanel.add(emoticonButton);
                    loadedImg.add(emoticonImg);
                }
            }
            if (loadedImg.size() > 0) getContentPane().add(leftPanel, java.awt.BorderLayout.WEST);
        }

        // load other icons
        try
        {
            iconFelix = new ImageIcon(getClass().getResource("icons/felix.gif")).getImage();
        }
        catch (final Exception e)
        {
            System.err.println("Can't find icons/felix.gif");
        }
        try
        {
            iconMessage = new ImageIcon(getClass().getResource("icons/message.gif")).getImage();
        }
        catch (final Exception e)
        {
            System.err.println("Can't find icons/message.gif");
        }

    }

    private void initSounds()
    {
        if (appletSounds)
        {
            AudioClip soundToLoad;
            for (int i = 0; i < soundList.length; i++)
            {
                try
                {
                    soundToLoad = Applet.newAudioClip(this.getClass().getResource(
                        "sounds/" + soundList[i] + ".wav"));
                    sounds.put(soundList[i], soundToLoad);
                    System.out.println("Sound '" + soundList[i] + "' loaded.");
                }
                catch (final Exception e)
                {
                    System.err.println("Sound '" + soundList[i] + "' not loaded!");
                }
            }
        }
        else
        {
            for (int i = 0; i < soundList.length; i++)
                sounds.put(soundList[i], new Integer(beepList[i]));
            System.out.println("Applet Sounds are OFF");
            chkSound.setText("Beep");
        }
    }

    private void resetchatBoard()
    {
        chatBoard = new StringBuffer();
        SwingUtilities.invokeLater(updateChatBoardDisplay);
    }

    private void playSound(final String toPlay)
    {
        if (chkSound.isSelected())
        {
            if (appletSounds)
            {
                final AudioClip soundToPlay = (AudioClip) sounds.get(toPlay);
                if (soundToPlay != null) soundToPlay.play();
            }
            else
            {
                for (int i = 0; i < ((Integer) sounds.get(toPlay)).intValue(); i++)
                {
                    if (i > 0)
                    {
                        try
                        {
                            Thread.sleep(150);
                        }
                        catch (final Exception e)
                        {
                        }
                    }

                    getToolkit().beep();
                }
            }
        }
    }

    private void appendHTMLMessage(final ChatMessage message,
                                   final String htmlColor,
                                   final String recipientList)
    {
        String htmlMessage = "<font size='1' color='gray'>" + message.getSender() + "@"
                             + df.format(message.getDateSent());
        // display list of recipients only if it is not a private or cast to all
        htmlMessage += recipientList;
        htmlMessage += "</font> <font size='3' color='" + htmlColor + "'><b>" + message.getContents()
                       + "<b></font><br>";
        chatBoard.append(htmlMessage);
        SwingUtilities.invokeLater(updateChatBoardDisplay);
    }

    @Override
    public void ShowMessage(final ChatMessage message)
    {
        if (message.getService().booleanValue())
        {
            // system messages
            appendHTMLMessage(message, "red", "");
        }
        else
        {
            if (message.getSender().equals(chatClient.getUserName()))
            {
                // feedback display of sent messages
                appendHTMLMessage(message, "gray",
                    (message.getRecipients().size() >= 1) ? message.getRecipients().toString() : "");
            }
            else
            {
                // this is a non-system non-feedback messages
                setTitleBar(true);

                if (message.getRecipients().size() == 0)
                {
                    // public messages
                    appendHTMLMessage(message, "black", "");
                    playSound("all");
                }
                else if (message.getRecipients().size() == 1)
                {
                    // private messages
                    appendHTMLMessage(message, "blue", "");
                    playSound("private");
                }
                else
                {
                    // group messages
                    appendHTMLMessage(message, "#DAAB00", message.getRecipients().toString());
                    playSound("group");
                }
            }
        }
    }

    @Override
    public void ShowListUsers(final Vector fullList)
    {
        // store the selected users so that we can reselect them after refresh
        final int nbUsersBefore = listUsers.getModel().getSize();
        final Vector selectedUsers = new Vector();
        final Object[] targets = listUsers.getSelectedValues();
        for (int i = 0; i < targets.length; i++)
        {
            selectedUsers.add(targets[i]);
        }
        // build a list of users excluding the current user
        final Vector filteredList = new Vector();
        String userName;
        for (final Enumeration e = fullList.elements(); e.hasMoreElements();)
        {
            userName = (String) (e.nextElement());
            if (!userName.equals(chatClient.getUserName())) filteredList.add(userName);
        }
        // display the list
        listUsers.setListData(filteredList);
        // reselect previously selected users
        String toReselect;
        for (final Enumeration e = selectedUsers.elements(); e.hasMoreElements();)
        {
            toReselect = (String) e.nextElement();
            for (int i = 0; i < listUsers.getModel().getSize(); i++)
            {
                if (((String) listUsers.getModel().getElementAt(i)).equals(toReselect))
                {
                    listUsers.addSelectionInterval(i, i);
                    break;
                }
            }
        }
        // play a sound depending if the list has grown or reduced
        if (listUsers.getModel().getSize() >= nbUsersBefore)
            playSound("joined");
        else
            playSound("left");

    }

    private String replace(final String text, final String searchText, String replaceText)
    {
        if (text == null || text.length() == 0 || searchText == null || searchText.length() == 0)
            return (text);

        if (replaceText == null) replaceText = new String("");

        int position = 0;

        StringBuffer textBuffer = new StringBuffer(text);

        while (true)
        {
            position = textBuffer.toString().indexOf(searchText, position);
            if (position < 0) break;

            textBuffer = textBuffer.replace(position, position + searchText.length(), replaceText);
            position += replaceText.length();
        }
        return textBuffer.toString();
    }

    private String parseEmoticons(final String message)
    {
        if (emoticons == null) return message;

        String result = message;

        final Enumeration emoticonKeys = emoticons.getKeys();
        String emoticonKey;
        while (emoticonKeys.hasMoreElements())
        {
            emoticonKey = (String) emoticonKeys.nextElement();
            result = replace(result, emoticonKey, "&nbsp;<img src='" + emoticons.getString(emoticonKey)
                                                  + "'>&nbsp;");
        }

        return result.toString();
    }

    private void SendMessage()
    {
        if (txtSend.getText().length() > 0)
        {
            final Vector addressees = new Vector();
            // append in the addressee vector the selected recipients
            // if every users or no users are selected -> message to all -> empty vector
            if ((listUsers.getSelectedIndices().length != listUsers.getModel().getSize())
                && (!listUsers.isSelectionEmpty()))
            {
                final Object[] targets = listUsers.getSelectedValues();
                for (int i = 0; i < targets.length; i++)
                {
                    addressees.add(targets[i]);
                }
            }
            // send the message
            if (chatClient.sendMessage(false, addressees, parseEmoticons(txtSend.getText()), "ack"))
            {
                playSound("sent");
                txtSend.setText("");
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    private void initComponents()
    {// GEN-BEGIN:initComponents
        splitPane = new javax.swing.JSplitPane();
        scrollMessages = new javax.swing.JScrollPane();
        editMessages = new javax.swing.JEditorPane();
        rightPanel = new javax.swing.JPanel();
        scrollListUsers = new javax.swing.JScrollPane();
        listUsers = new javax.swing.JList();
        lblUserName = new javax.swing.JLabel();
        btnSelect = new javax.swing.JButton();
        topPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        lblSent = new javax.swing.JLabel();
        lblAll = new javax.swing.JLabel();
        lblGroup = new javax.swing.JLabel();
        lblPrivate = new javax.swing.JLabel();
        lblSystem = new javax.swing.JLabel();
        chkSound = new javax.swing.JCheckBox();
        bottomPanel = new javax.swing.JPanel();
        btnSend = new javax.swing.JButton();
        txtSend = new javax.swing.JTextField();

        setTitle("Felix");
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        setFont(new java.awt.Font("Arial", 0, 10));
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(final java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        splitPane.setDividerLocation(450);
        splitPane.setPreferredSize(new java.awt.Dimension(600, 400));
        splitPane.setMinimumSize(new java.awt.Dimension(600, 400));
        splitPane.setOneTouchExpandable(true);
        scrollMessages.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMessages.setDoubleBuffered(true);
        editMessages.setEditable(false);
        editMessages.setDoubleBuffered(true);
        editMessages.setContentType("text/html");
        scrollMessages.setViewportView(editMessages);

        splitPane.setLeftComponent(scrollMessages);

        rightPanel.setLayout(new java.awt.BorderLayout());

        scrollListUsers.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollListUsers.setViewportView(listUsers);

        rightPanel.add(scrollListUsers, java.awt.BorderLayout.CENTER);

        lblUserName.setText("Username");
        lblUserName.setForeground(java.awt.Color.white);
        lblUserName.setBackground(new java.awt.Color(0, 153, 102));
        lblUserName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUserName.setFont(new java.awt.Font("Dialog", 1, 14));
        lblUserName.setPreferredSize(new java.awt.Dimension(100, 27));
        lblUserName.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        lblUserName.setMinimumSize(new java.awt.Dimension(100, 27));
        lblUserName.setMaximumSize(new java.awt.Dimension(100, 27));
        lblUserName.setOpaque(true);
        lblUserName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rightPanel.add(lblUserName, java.awt.BorderLayout.NORTH);

        btnSelect.setText("Selection");
        btnSelect.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt)
            {
                btnSelectActionPerformed(evt);
            }
        });

        rightPanel.add(btnSelect, java.awt.BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);

        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

        jButton1.setLabel("Reset");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        topPanel.add(jButton1);

        lblSent.setText("Sent");
        lblSent.setForeground(java.awt.Color.white);
        lblSent.setBackground(java.awt.Color.gray);
        lblSent.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSent.setPreferredSize(new java.awt.Dimension(65, 27));
        lblSent.setMinimumSize(new java.awt.Dimension(65, 27));
        lblSent.setMaximumSize(new java.awt.Dimension(65, 27));
        lblSent.setOpaque(true);
        lblSent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblSent.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(final java.awt.event.MouseEvent evt)
            {
                lblSentMouseReleased(evt);
            }
        });

        topPanel.add(lblSent);

        lblAll.setText("Public");
        lblAll.setForeground(java.awt.Color.white);
        lblAll.setBackground(java.awt.Color.black);
        lblAll.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAll.setPreferredSize(new java.awt.Dimension(65, 27));
        lblAll.setMinimumSize(new java.awt.Dimension(65, 27));
        lblAll.setMaximumSize(new java.awt.Dimension(65, 27));
        lblAll.setOpaque(true);
        lblAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblAll.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(final java.awt.event.MouseEvent evt)
            {
                lblAllMouseReleased(evt);
            }
        });

        topPanel.add(lblAll);

        lblGroup.setText("Group");
        lblGroup.setForeground(java.awt.Color.black);
        lblGroup.setBackground(java.awt.Color.orange);
        lblGroup.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGroup.setPreferredSize(new java.awt.Dimension(65, 27));
        lblGroup.setMinimumSize(new java.awt.Dimension(65, 27));
        lblGroup.setMaximumSize(new java.awt.Dimension(65, 27));
        lblGroup.setOpaque(true);
        lblGroup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblGroup.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(final java.awt.event.MouseEvent evt)
            {
                lblGroupMouseReleased(evt);
            }
        });

        topPanel.add(lblGroup);

        lblPrivate.setText("Private");
        lblPrivate.setForeground(java.awt.Color.white);
        lblPrivate.setBackground(java.awt.Color.blue);
        lblPrivate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPrivate.setPreferredSize(new java.awt.Dimension(65, 27));
        lblPrivate.setMinimumSize(new java.awt.Dimension(65, 27));
        lblPrivate.setMaximumSize(new java.awt.Dimension(65, 27));
        lblPrivate.setOpaque(true);
        lblPrivate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblPrivate.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(final java.awt.event.MouseEvent evt)
            {
                lblPrivateMouseReleased(evt);
            }
        });

        topPanel.add(lblPrivate);

        lblSystem.setText("System");
        lblSystem.setForeground(java.awt.Color.white);
        lblSystem.setBackground(java.awt.Color.red);
        lblSystem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSystem.setPreferredSize(new java.awt.Dimension(65, 27));
        lblSystem.setMinimumSize(new java.awt.Dimension(65, 27));
        lblSystem.setMaximumSize(new java.awt.Dimension(65, 27));
        lblSystem.setOpaque(true);
        lblSystem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblSystem.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(final java.awt.event.MouseEvent evt)
            {
                lblSystemMouseReleased(evt);
            }
        });

        topPanel.add(lblSystem);

        chkSound.setSelected(true);
        chkSound.setText("Sound");
        topPanel.add(chkSound);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        bottomPanel.setLayout(new java.awt.BorderLayout(5, 0));

        btnSend.setText("Send");
        btnSend.setPreferredSize(new java.awt.Dimension(65, 27));
        btnSend.setBorder(new javax.swing.border.EtchedBorder());
        btnSend.setMaximumSize(new java.awt.Dimension(65, 27));
        btnSend.setMinimumSize(new java.awt.Dimension(65, 27));
        btnSend.setEnabled(false);
        btnSend.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt)
            {
                btnSendActionPerformed(evt);
            }
        });

        bottomPanel.add(btnSend, java.awt.BorderLayout.EAST);

        txtSend.setBorder(new javax.swing.border.EtchedBorder(java.awt.Color.darkGray,
            java.awt.Color.lightGray));
        txtSend.setEnabled(false);
        txtSend.addKeyListener(new java.awt.event.KeyAdapter()
        {
            @Override
            public void keyReleased(final java.awt.event.KeyEvent evt)
            {
                txtSendKeyReleased(evt);
            }
        });

        bottomPanel.add(txtSend, java.awt.BorderLayout.CENTER);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

    }// GEN-END:initComponents

    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_jButton1ActionPerformed
        setTitleBar(false);
        resetchatBoard();
    }// GEN-LAST:event_jButton1ActionPerformed

    private void lblSystemMouseReleased(final java.awt.event.MouseEvent evt)
    {// GEN-FIRST:event_lblSystemMouseReleased
        playSound("alarm");
    }// GEN-LAST:event_lblSystemMouseReleased

    private void lblPrivateMouseReleased(final java.awt.event.MouseEvent evt)
    {// GEN-FIRST:event_lblPrivateMouseReleased
        playSound("private");
    }// GEN-LAST:event_lblPrivateMouseReleased

    private void lblGroupMouseReleased(final java.awt.event.MouseEvent evt)
    {// GEN-FIRST:event_lblGroupMouseReleased
        playSound("group");
    }// GEN-LAST:event_lblGroupMouseReleased

    private void lblAllMouseReleased(final java.awt.event.MouseEvent evt)
    {// GEN-FIRST:event_lblAllMouseReleased
        playSound("all");
    }// GEN-LAST:event_lblAllMouseReleased

    private void lblSentMouseReleased(final java.awt.event.MouseEvent evt)
    {// GEN-FIRST:event_lblSentMouseReleased
        playSound("sent");
    }// GEN-LAST:event_lblSentMouseReleased

    private void btnSelectActionPerformed(final java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_btnSelectActionPerformed
        setTitleBar(false);
        if (toggleSelection)
        {
            listUsers.setSelectionInterval(0, listUsers.getModel().getSize() - 1);
        }
        else
        {
            listUsers.clearSelection();
        }
        toggleSelection = !toggleSelection;
    }// GEN-LAST:event_btnSelectActionPerformed

    private void txtSendKeyReleased(final java.awt.event.KeyEvent evt)
    {// GEN-FIRST:event_txtSendKeyReleased
        setTitleBar(false);
        if ((evt.getSource() == txtSend) && (evt.getKeyCode() == evt.VK_ENTER)) SendMessage();
    }// GEN-LAST:event_txtSendKeyReleased

    private void btnSendActionPerformed(final java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_btnSendActionPerformed
        setTitleBar(false);
        SendMessage();
    }// GEN-LAST:event_btnSendActionPerformed

    /** Exit the Application */
    private void exitForm(final java.awt.event.WindowEvent evt)
    {// GEN-FIRST:event_exitForm
        System.exit(chatClient.requestLogout() ? 0 : 1);
    }// GEN-LAST:event_exitForm

    /**
     * Launch the GUI client. Expected arguments : {$userName} {$hostNameOrIP} {$hostPortNumber}
     * 
     * @param args the command line arguments
     */
    public static void main(final String args[]) throws Exception
    {
        if (args.length >= 4)
        {
            if (args[3].equals("-noAppletSounds"))
            {
                appletSounds = false;
            }
        }

        guiClient = new Client(args[0], args[1], Integer.parseInt(args[2]));
        guiClient.show();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JScrollPane scrollMessages;
    private javax.swing.JEditorPane editMessages;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JScrollPane scrollListUsers;
    private javax.swing.JList listUsers;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JButton btnSelect;
    private javax.swing.JPanel topPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel lblSent;
    private javax.swing.JLabel lblAll;
    private javax.swing.JLabel lblGroup;
    private javax.swing.JLabel lblPrivate;
    private javax.swing.JLabel lblSystem;
    private javax.swing.JCheckBox chkSound;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton btnSend;
    private javax.swing.JTextField txtSend;
    // End of variables declaration//GEN-END:variables

}
