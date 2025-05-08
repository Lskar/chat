// ChatJFrame.java

package org.chatTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatJFrame extends JFrame {
    private int userId;
    private JTextArea chatArea;
    private JTextField inputField;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ChatJFrame(int userId) {
        this.userId = userId;

        setTitle("聊天室 - 用户ID: " + userId);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭窗口时释放资源
        setLocationRelativeTo(null); // 居中显示

        // 初始化界面组件
        initializeUI();

        // 显示欢迎信息
        showWelcomeMessage();

        // 连接服务器并启动消息接收线程
        connectToServer();
        new Thread(this::receiveMessages).start();
    }

    private void initializeUI() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 聊天内容显示区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("发送");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        this.add(mainPanel);
    }

    private void showWelcomeMessage() {
        chatArea.append("欢迎回来，用户ID: " + userId + "！\n");
        chatArea.append("请输入消息开始聊天...\n");
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("127.0.0.1", 8880);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 登录成功后发送 userId 给服务器确认身份
            out.writeObject("USER_ID:" + userId);
            out.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法连接到服务器: " + e.getMessage(), "连接错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            try {
                out.writeObject("MESSAGE:" + userId + ":" + message);
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
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof String) {
                    String response = (String) obj;
                    if (response.startsWith("MESSAGE:")) {
                        String[] parts = response.split(":", 3);
                        if (parts.length == 3) {
                            int fromUserId = Integer.parseInt(parts[1]);
                            String msg = parts[2];
                            if (fromUserId != userId) {
                                chatArea.append("用户 " + fromUserId + ": " + msg + "\n");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "服务器断开连接: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatJFrame chatJFrame = new ChatJFrame(1);
            chatJFrame.setVisible(true);
        });
    }
}
