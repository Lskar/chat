package org.chatTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

public class FriendListFrame extends JFrame {
    private int currentUserId;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public FriendListFrame(int userId, String[] friends, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.currentUserId = userId;
        this.socket = socket;
        this.in = in;
        this.out = out;

        setTitle("好友列表");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI(friends);
        setVisible(true);
    }

    private void initializeUI(String[] friends) {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        for (String friend : friends) {
            JLabel label = new JLabel("👤 " + friend);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isPortAvailable(currentUserId)) {
                        int friendId = Integer.parseInt(friend.replace("User", ""));
                        new ChatWindow(currentUserId, friendId);
                    }
                    else {
                        JOptionPane.showMessageDialog(label, "与该好友的聊天窗口已打开", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            panel.add(label);
        }

        add(panel);
    }
    public static boolean isPortAvailable(int port) {
        try (DatagramSocket serverSocket = new DatagramSocket(port+9999)) {
            serverSocket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
