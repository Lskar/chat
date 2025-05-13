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
    private Thread udpListener;
    private volatile boolean isRunning = true;

    public ChatWindow(int selfId, int targetId) {
        this.selfId = selfId;
        this.targetId = targetId;
        this.udpSocket = createUDPSocket();

        setTitle("我(id"+selfId+")与 用户" + targetId + " 的聊天");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 窗口关闭前,在这里实现要处理的操作
                udpListener.interrupt();//无法关闭线程
                udpSocket.close();
                super.windowClosing(e);
            }
        });
        initializeUI();
        startUDPListener();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopUDPListener();
            }
        });
    }

    private DatagramSocket createUDPSocket() {
        try {
            return new DatagramSocket(9999 + selfId*10+targetId); // 每个用户绑定不同端口,,暂时这样分配
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
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9999 + targetId*10+selfId);
                udpSocket.send(packet);
                System.out.println("send message to friend: " + fullMessage);
                chatArea.append("我 (" + selfId + ")：" + message + "\n");
                saveMessageToFile(selfId, targetId, message, true);
                inputField.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "发送失败: " + ex.getMessage());
            }
        }
    }


    private void startUDPListener() {
        udpListener = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (isRunning) { // 检查标志位
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    udpSocket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("receive" + received);
                    if (received.startsWith("MESSAGE")) {
                        String[] parts = received.split(":", 4);
                        int from = Integer.parseInt(parts[1]);
                        int to = Integer.parseInt(parts[2]);
                        String msg = parts[3];

                        if (to == selfId && from == targetId) {
                            SwingUtilities.invokeLater(() -> {
                                chatArea.append("用户 " + from + ": " + msg + "\n");
                            });
                        }
                    }
                } catch (SocketException e) {
                    if (isRunning) { // 如果标志位为 true，说明是异常关闭
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "UDP 监听异常: " + e.getMessage());
                    }
                    break; // 退出循环
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "UDP 监听异常: " + e.getMessage());
                    break; // 退出循环
                }
            }
        });
        udpListener.start();
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
    private void stopUDPListener() {
        isRunning = false; // 设置标志位为 false
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close(); // 关闭 udpSocket
        }
        if (udpListener != null) {
            try {
                udpListener.join(); // 等待监听线程结束
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}