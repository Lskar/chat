package org.chatTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class ChatJFrame extends JFrame {
    private int userId;
    private JTextArea chatArea;
    private JTextField inputField;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    // 新增好友列表组件
    private DefaultListModel<String> friendListModel = new DefaultListModel<>();
    private JList<String> friendList = new JList<>(friendListModel);
    private Map<Integer, PrivateChatFrame> privateChats = new HashMap<>();

    public ChatJFrame(int userId, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.userId = userId;
        this.socket = socket;
        this.in = in;
        this.out = out;

        setTitle("聊天室 - 用户ID: " + userId);
        setSize(800, 400); // 加宽窗口
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();
        showWelcomeMessage();

        new Thread(this::receiveMessages).start();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 原有聊天组件
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        // 聊天区域样式
        chatArea = new JTextArea();
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("发送");
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // 新增好友面板
        JPanel friendPanel = createFriendPanel();
        mainPanel.add(friendPanel, BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createFriendPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 设置面板尺寸和间距
        panel.setPreferredSize(new Dimension(220, 400)); // 加宽面板
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 四周间距
        panel.setBackground(new Color(213, 216, 220)); // 背景

        // 搜索组件
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(new Color(213, 216, 220));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // 底部间距
        JTextField searchField = new JTextField();
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK), // 修改为黑色边框
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchField.setBackground(new Color(253, 254, 254));
        searchField.setForeground(Color.BLACK);
        searchField.setFont(new Font("宋体", Font.PLAIN, 14));

        JButton addButton = new JButton("添加");
        addButton.setBackground(new Color(253, 254, 254));
        addButton.setForeground(Color.BLACK);
        addButton.setFont(new Font("宋体", Font.BOLD, 14)); // 设置加粗宋体
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK), // 添加边框
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        addButton.addActionListener(e -> handleAddFriend(searchField.getText()));

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(addButton, BorderLayout.EAST);

        // 好友列表样式
        friendList.setBackground(new Color(244, 246, 246 ));
        friendList.setForeground(Color.WHITE);
        friendList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 自定义列表渲染器
        friendList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // 列表项边距
                label.setBackground(isSelected ? new Color(178, 186, 187) : new Color(178, 186, 187));
                label.setForeground(Color.WHITE);
                return label;
            }
        });

        // 滚动面板设置
        JScrollPane scrollPane = new JScrollPane(friendList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }


    private void handleAddFriend(String input) {
        try {
            int friendId = Integer.parseInt(input);
            if (friendId != userId && !friendListModel.contains(input)) {
                out.writeObject("ADD_FRIEND:" + userId + ":" + friendId);
                out.flush();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "无效的用户ID", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openPrivateChat(int friendId) {
        if (!privateChats.containsKey(friendId)) {
            PrivateChatFrame frame = new PrivateChatFrame(friendId);
            privateChats.put(friendId, frame);
        }
        privateChats.get(friendId).setVisible(true);
    }

    private class PrivateChatFrame extends JFrame {
        private final int friendId;
        private JTextArea chatArea = new JTextArea();
        private JTextField inputField = new JTextField();

        public PrivateChatFrame(int friendId) {
            this.friendId = friendId;
            setTitle("私聊 - " + friendId);
            setSize(300, 200);

            JPanel panel = new JPanel(new BorderLayout());
            chatArea.setEditable(false);
            panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
            // 设置私聊窗口样式
            chatArea.setBackground(Color.WHITE);
            chatArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            inputField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));

            JPanel inputPanel = new JPanel(new BorderLayout());
            JButton sendButton = new JButton("发送");
            sendButton.addActionListener(e -> sendPrivateMessage());
            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            panel.add(inputPanel, BorderLayout.SOUTH);
            add(panel);
        }

        private void sendPrivateMessage() {
            String message = inputField.getText();
            if (!message.isEmpty()) {
                try {
                    out.writeObject("PRIVATE:" + userId + ":" + friendId + ":" + message);
                    out.flush();
                    chatArea.append("我: " + message + "\n");
                    inputField.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void appendMessage(String message) {
            chatArea.append(friendId + ": " + message + "\n");
        }
    }

    private void showWelcomeMessage() {
        chatArea.append("欢迎回来，用户ID: " + userId + "！\n");
        chatArea.append("请输入消息开始聊天...\n");
    }

    private void sendMessage() {//发送至服务器，让服务器进行转发
        String message = inputField.getText();
        if (!message.isEmpty()) {
            try {
                System.out.println("myid sendmessage  "+userId+message);////////////
                out.writeObject("MESSAGE:" + userId + ":" + message);
                out.flush(); // ⚠️ 每次都要 flush
                chatArea.append("我 (" + userId + "): " + message + "\n");
                inputField.setText("");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "消息发送失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void receiveMessages() {
        try {
            while (!socket.isClosed()) {
                MulticastSocket ms = new MulticastSocket(10000);
                byte[] bytes = new byte[1024];
                DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
                ms.receive(dp);

                String response = new String(dp.getData(), 0, dp.getLength());
                System.out.println("收到消息: " + response);

                // 处理公共消息
                if (response.startsWith("MESSAGE:")) {
                    String[] parts = response.split(":", 3);
                    if (parts.length == 3) {
                        int fromUserId = Integer.parseInt(parts[1]);
                        String msg = parts[2];
                        if (fromUserId != userId) {
                            SwingUtilities.invokeLater(() ->
                                    chatArea.append("用户 " + fromUserId + ": " + msg + "\n")
                            );
                        }
                    }
                }
                // 处理好友添加响应
                else if (response.startsWith("FRIEND_ADDED:")) {
                    String[] parts = response.split(":");
                    SwingUtilities.invokeLater(() ->
                            friendListModel.addElement(parts[1])
                    );
                }
                // 处理私聊消息
                else if (response.startsWith("PRIVATE:")) {
                    String[] parts = response.split(":", 4);
                    int fromUserId = Integer.parseInt(parts[1]);
                    String message = parts[3];

                    SwingUtilities.invokeLater(() -> {
                        if (!privateChats.containsKey(fromUserId)) {
                            openPrivateChat(fromUserId);
                        }
                        privateChats.get(fromUserId).appendMessage(message);
                    });
                }

                ms.close();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "服务器断开连接: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            closeResources();
        }
    }

    private void closeResources() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}