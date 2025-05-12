package org.chatTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.Date;

public class ChatWindow extends JFrame {

    private int selfId;
    private int targetId;
    private JTextArea chatArea;
    private JTextField inputField;
    private DatagramSocket udpSocket;

    public ChatWindow(int selfId, int targetId) {
        this.selfId = selfId;
        this.targetId = targetId;
        this.udpSocket = createUDPSocket();

        setTitle("与 用户" + targetId + " 的聊天");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 窗口关闭前,在这里实现要处理的操作
                udpSocket.close();
                super.windowClosing(e);
            }
        });
        initializeUI();
        startUDPListener();
    }

    private DatagramSocket createUDPSocket() {
        try {
            return new DatagramSocket(9999 + selfId); // 每个用户绑定不同端口
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeUI() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        JButton sendButton = new JButton("发送");

        sendButton.addActionListener(this::sendMessage);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void sendMessage(ActionEvent e) {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            String fullMessage = "MESSAGE:" + selfId + ":" + targetId + ":" + message;
            byte[] buffer = fullMessage.getBytes();
            try {
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9999 + targetId);
                udpSocket.send(packet);
                chatArea.append("我 (" + selfId + ")：" + message + "\n");
                saveMessageToFile(selfId, targetId, message, true);
                inputField.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "发送失败: " + ex.getMessage());
            }
        }
    }

    private void startUDPListener() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (!udpSocket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    udpSocket.setSoTimeout(100); // 设置超时时间，避免无限阻塞
                    udpSocket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    if (received.startsWith("MESSAGE")) {
                        String[] parts = received.split(":", 4);
                        int from = Integer.parseInt(parts[1]);
                        int to = Integer.parseInt(parts[2]);
                        String msg = parts[3];

                        if (to == selfId && from == targetId) {
                            SwingUtilities.invokeLater(() -> {
                                chatArea.append("用户 " + from + ": " + msg + "\n");
                            });
                            saveMessageToFile(from, to, msg, false);
                        }
                    }
                } catch (SocketTimeoutException ex) {
                    // 超时，继续循环
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    private void saveMessageToFile(int from, int to, String message, boolean isSend) {
        String filename = "chat_history_" + Math.min(from, to) + "_" + Math.max(from, to) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write((isSend ? "我 (" + from + ")" : "对方 (" + from + ")") + ": " + message + " [" + new Date() + "]");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
