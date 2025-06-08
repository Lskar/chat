package org.chatTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatRoomJFrame extends JFrame {
    private int userId;
    private JTextArea chatArea;
    private JTextField inputField;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public ChatRoomJFrame(int userId, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.userId = userId;
        this.socket = socket;
        this.in = in;
        this.out = out;

        setTitle("聊天室 - 用户ID: " + userId);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();
        showWelcomeMessage();

        new Thread(this::receiveMessages).start();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("发送");

        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void showWelcomeMessage() {
        chatArea.append("欢迎回来，用户ID: " + userId + "！\n");
        chatArea.append("请输入消息开始聊天...\n");
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            try {
                out.writeObject("MESSAGE:" + userId + ":" + message);
                out.flush(); //每次都要 flush
                System.out.println("send message to server: " + message);
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
                Object obj = in.readObject();
                if (obj instanceof String response) {
                    System.out.println("receive message from server: " + response);
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
                }
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