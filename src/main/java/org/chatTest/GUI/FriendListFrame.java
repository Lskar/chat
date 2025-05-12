package org.chatTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FriendListFrame extends JFrame {
    private int currentUserId;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private static Map<Integer, ChatWindow> openChatWindows = new HashMap<>();

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
            JLabel label = new JLabel("好友: " + friend);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int friendId = Integer.parseInt(friend.replace("User", ""));

                    // 检查是否已经打开过这个好友的聊天窗口
                    if (openChatWindows.containsKey(friendId)) {
                        JOptionPane.showMessageDialog(label, "与该好友的聊天窗口已打开", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // 创建新窗口并加入记录
                    ChatWindow chatWindow = new ChatWindow(currentUserId, friendId);
                    openChatWindows.put(friendId, chatWindow);

                    // 添加窗口监听器，在窗口关闭时移除记录
                    chatWindow.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            openChatWindows.remove(friendId);
                        }
                    });
                }
            });
            panel.add(label);
        }
        add(panel);
    }

}
