package org.chatTest.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
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
    private JTextField searchField; // 提升为类变量

    public FriendListFrame(int userId, String[] friends, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.currentUserId = userId;
        this.socket = socket;
        this.in = in;
        this.out = out;

        setTitle("我（id"+currentUserId+"）的好友列表");
        setSize(300, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(new Color(237, 237, 237));

        initializeUI(friends);
        setVisible(true);
    }

    private void initializeUI(String[] friends) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 顶部操作面板
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 好友列表
        JPanel friendListPanel = createFriendList(friends);
        JScrollPane scrollPane = new JScrollPane(friendListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部控制面板
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(237, 237, 237));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(220, 220, 220)),
                new EmptyBorder(5, 5, 5, 5))
        );

        searchField = new JTextField("输入好友ID");
        searchField.setForeground(Color.GRAY);
        searchField.setBorder(BorderFactory.createEmptyBorder());

        // 搜索框焦点监听
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

        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton addButton = new JButton("+");
        addButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        addButton.setContentAreaFilled(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        addButton.addActionListener(e -> performAddFriend());

        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createFriendList(String[] friends) {
        JPanel friendListPanel = new JPanel();
        friendListPanel.setLayout(new BoxLayout(friendListPanel, BoxLayout.Y_AXIS));
        friendListPanel.setBackground(Color.WHITE);

        for (String friend : friends) {
            JPanel friendEntry = createFriendEntry(friend);
            friendListPanel.add(friendEntry);
        }
        return friendListPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        bottomPanel.setBackground(Color.WHITE);

        // 退出登录按钮
        JButton logoutButton = new JButton("退出登录");
        styleButton(logoutButton, Color.GRAY);
        logoutButton.addActionListener(e -> {
            try {
                socket.close();
                dispose();
                // 这里可以添加返回登录界面的逻辑
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 更新列表按钮
        JButton refreshButton = new JButton("更新列表");
        styleButton(refreshButton, Color.GRAY);
        refreshButton.addActionListener(e -> {
            // 这里添加更新好友列表的逻辑
            JOptionPane.showMessageDialog(this, "正在更新好友列表...");
            // 示例：重新获取好友列表并更新界面
            // refreshFriendList();
        });

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
                new EmptyBorder(8, 15, 8, 15))
        );
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
            // 这里添加实际添加好友的逻辑
            System.out.println("正在添加好友: " + friendId);
            // 示例：通过out流发送添加请求
            // out.writeObject(new AddFriendRequest(currentUserId, friendId));

            JOptionPane.showMessageDialog(this, "好友请求已发送", "成功", JOptionPane.INFORMATION_MESSAGE);
            searchField.setText("输入好友ID");
            searchField.setForeground(Color.GRAY);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "无效的用户ID格式", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createFriendEntry(String friend) {
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBackground(Color.WHITE); // 修复1：改回白色背景
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                new EmptyBorder(10, 15, 10, 15))
        );
        entryPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 头像和用户名
        JLabel avatarLabel = new JLabel(new ImageIcon("path/to/avatar.png"));
        avatarLabel.setPreferredSize(new Dimension(40, 40));

        JLabel nameLabel = new JLabel(friend);
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        nameLabel.setForeground(Color.BLACK); // 明确设置文字颜色

        // 修复2：正确添加所有组件
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.add(avatarLabel, BorderLayout.WEST);
        contentPanel.add(nameLabel, BorderLayout.CENTER); // 添加缺失的nameLabel
        contentPanel.setOpaque(false);

        entryPanel.add(contentPanel, BorderLayout.CENTER);

        // 鼠标悬停效果
        entryPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                entryPanel.setBackground(new Color(240, 240, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                entryPanel.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int friendId = Integer.parseInt(friend.replace("User", ""));
                if (isPortAvailable(currentUserId,friendId)) {
                    new ChatWindow(currentUserId, friendId);
                } else {
                    JOptionPane.showMessageDialog(entryPanel, "与该好友的聊天窗口已打开", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return entryPanel;
    }

    private void showAddFriendDialog() {
        // 添加好友的具体逻辑
        String friendId = JOptionPane.showInputDialog(this, "请输入好友ID:", "添加好友", JOptionPane.PLAIN_MESSAGE);
        if (friendId != null && !friendId.trim().isEmpty()) {
            // 这里添加实际添加好友的逻辑
            System.out.println("尝试添加好友: " + friendId);
        }
    }

    private static boolean isPortAvailable(int selfId,int friendId) {
        try (DatagramSocket serverSocket = new DatagramSocket(selfId*10+friendId + 9999)) {
            serverSocket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}