package org.chatTest.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
    private JTextField searchField;

    private JPanel friendListPanel;
    private Map<String, Boolean> friendStatusMap = new HashMap<>();

    public FriendListFrame(int userId, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.currentUserId = userId;
        this.socket = socket;
        this.in = in;
        this.out = out;

        setTitle("我（id" + currentUserId + "）的好友列表");
        setSize(300, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(new Color(237, 237, 237));

        initializeUI();
        setVisible(true);

        new Thread(this::startListeningForMessages).start();
        requestChangeStatus("online");
        requestFriendsList();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 顶部搜索面板
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 好友列表面板
        friendListPanel = new JPanel();
        friendListPanel.setLayout(new BoxLayout(friendListPanel, BoxLayout.Y_AXIS));
        friendListPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(friendListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(237, 237, 237));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 搜索框
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField("输入好友ID");
        searchField.setForeground(Color.GRAY);
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("输入好友ID")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("输入好友ID");
                }
            }
        });

        JButton addButton = new JButton("+");
        addButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        addButton.setContentAreaFilled(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        addButton.addActionListener(e -> performAddFriend());

        searchPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        bottomPanel.setBackground(Color.WHITE);

        JButton logoutButton = new JButton("退出登录");
        JButton refreshButton = new JButton("更新列表");

        styleButton(logoutButton, Color.GRAY);
        styleButton(refreshButton, Color.GRAY);

        logoutButton.addActionListener(e -> {
            try {
                requestChangeStatus("offline");
                socket.close();
                dispose();
                System.exit(1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        refreshButton.addActionListener(e -> requestFriendsList());

        bottomPanel.add(logoutButton);
        bottomPanel.add(refreshButton);
        return bottomPanel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, bgColor.darker()),
                new EmptyBorder(8, 15, 8, 15)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void performAddFriend() {
        String input = searchField.getText().trim();
        if (input.isEmpty() || input.equals("输入好友ID")) {
            JOptionPane.showMessageDialog(this, "请输入有效的好友ID", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int friendId = Integer.parseInt(input);
            out.writeObject("ADD_FRIEND:" + currentUserId + ":" + friendId);
            out.flush();
            JOptionPane.showMessageDialog(this, "好友请求已发送", "成功", JOptionPane.INFORMATION_MESSAGE);
            searchField.setText("输入好友ID");
            searchField.setForeground(Color.GRAY);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "无效的用户ID格式", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void startListeningForMessages() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof String message) {
                    if (message.startsWith("FRIENDS:")) {
                        updateFriendsList(message);
//                        updateFriendStatus(message);
                    } else if (message.startsWith("STATUS:")) {
//                        updateFriendStatus(message);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestFriendsList() {
        try {
            out.writeObject("REQUEST_FRIENDS:" + currentUserId);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void requestChangeStatus(String status){
        try {
            out.writeObject("CHANGE_STATUS:" + currentUserId+":"+status);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateFriendsList(String message) {
        String[] parts = message.split(":", 2);
        String[] friendStatusPairs = parts[1].split(",");
        friendListPanel.removeAll();

        for (String pair : friendStatusPairs) {
            String[] data = pair.split(":", 2); // 分割好友名和状态
            String friendName = data[0];
            boolean isOnline = false;

            // 新增：处理状态部分
            if (data.length > 1 && "online".equalsIgnoreCase(data[1])) {
                isOnline = true;
            } else if (data.length > 1 && "offline".equalsIgnoreCase(data[1])) {
                isOnline = false;
            }

            friendStatusMap.put(friendName, isOnline);
            friendListPanel.add(createFriendEntry(friendName, isOnline));
        }

        friendListPanel.revalidate();
        friendListPanel.repaint();
    }

    private void updateFriendStatus(String message) {
        String[] parts = message.split(":", 3);
        String friendName = parts[1];
        boolean isOnline = Boolean.parseBoolean(parts[2]);
        friendStatusMap.put(friendName, isOnline);
        updateFriendsList("FRIENDS:" + String.join(",", friendStatusMap.keySet()));
    }

    private JPanel createFriendEntry(String friendName, boolean isOnline) {
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBackground(Color.WHITE);
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                new EmptyBorder(10, 15, 10, 15)));
        entryPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel(friendName);
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        nameLabel.setForeground(Color.BLACK);

        JLabel statusLabel = new JLabel(isOnline ? "在线" : "离线");
        statusLabel.setForeground(isOnline ? Color.GREEN : Color.RED);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.add(nameLabel, BorderLayout.WEST);
        contentPanel.add(statusLabel, BorderLayout.EAST);
        contentPanel.setOpaque(false);

        entryPanel.add(contentPanel, BorderLayout.CENTER);

        entryPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int friendId = Integer.parseInt(friendName.replace("User", ""));
                if (isPortAvailable(currentUserId, friendId)) {
                    new ChatWindow(currentUserId, friendId);
                } else {
                    JOptionPane.showMessageDialog(entryPanel, "与该好友的聊天窗口已打开", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                entryPanel.setBackground(new Color(240, 240, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                entryPanel.setBackground(Color.WHITE);
            }
        });

        return entryPanel;
    }

    private static boolean isPortAvailable(int selfId, int friendId) {
        try (DatagramSocket serverSocket = new DatagramSocket(selfId * 10 + friendId + 9999)) {
            serverSocket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}