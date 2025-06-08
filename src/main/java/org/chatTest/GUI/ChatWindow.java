package org.chatTest.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Base64;
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
                stopUDPListener();
                udpSocket.close();
                super.windowClosing(e);
            }
        });
        initializeUI();
        startUDPListener();
    }

    private DatagramSocket createUDPSocket() {
        try {
            DatagramSocket socket = new DatagramSocket(9999 + selfId*10+targetId);
            socket.setReceiveBufferSize(1024 * 1024); // 增加接收缓冲区
            return socket;
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
        JButton historyButton = new JButton("历史");
        JButton fileButton = new JButton("文件"); // 新增文件按钮

        sendButton.addActionListener(this::sendMessage);
        historyButton.addActionListener(e -> loadHistoryMessages());
        fileButton.addActionListener(e -> sendFile()); // 绑定文件发送

        JPanel inputPanel = new JPanel(new BorderLayout());

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(historyButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(fileButton); // 添加文件按钮到面板

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

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
                chatArea.append("我 (" + selfId + ")：" + message + "\n");
                saveMessageToFile(selfId, targetId, message, true);
                saveMessageToFile(targetId, selfId, message, false);
                inputField.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "发送失败: " + ex.getMessage());
            }
        }
    }

    private void startUDPListener() {
        udpListener = new Thread(() -> {
            byte[] buffer = new byte[1024 * 1024]; // 1MB缓冲区
            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
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
                        }
                    }
                    // 新增文件接收处理
                    else if (received.startsWith("FILE")) {
                        String[] parts = received.split(":", 5);
                        int from = Integer.parseInt(parts[1]);
                        int to = Integer.parseInt(parts[2]);
                        String fileName = parts[3];
                        byte[] fileData = Base64.getDecoder().decode(parts[4]);

                        if (to == selfId && from == targetId) {
                            saveReceivedFile(fileName, fileData);
                            SwingUtilities.invokeLater(() ->
                                    chatArea.append("收到文件: " + fileName + "\n"));
                        }
                    }
                } catch (SocketException e) {
                    if (isRunning) {
                        JOptionPane.showMessageDialog(this, "UDP监听异常: " + e.getMessage());
                    }
                    break;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "UDP监听异常: " + e.getMessage());
                    break;
                }
            }
        });
        udpListener.start();
    }

    // 新增文件发送方法
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                String fileName = selectedFile.getName();

                // 构造文件传输消息
                String message = "FILE:" + selfId + ":" + targetId + ":" + fileName + ":" + Base64.getEncoder().encodeToString(fileData);
                byte[] buffer = message.getBytes();

                // 使用临时Socket发送
                try (DatagramSocket sendSocket = new DatagramSocket()) {
                    InetAddress address = InetAddress.getByName("localhost");
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                            address, 9999 + targetId*10 + selfId);
                    sendSocket.send(packet);
                }

                chatArea.append("已发送文件: " + fileName + "\n");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "文件发送失败: " + ex.getMessage());
            }
        }
    }

    // 新增文件保存方法
    private void saveReceivedFile(String fileName, byte[] fileData) {
        // ✅ 使用 selfId 构建路径，确保文件保存在接收方的目录下
        String storagePath = "STORAGE/user" + selfId + "/received/";

        File dir = new File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(storagePath + fileName)) {
            fos.write(fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMessageToFile(int from, int to, String message, boolean isSend) {
        String storageRoot = "STORAGE";
        String userDir = storageRoot + "/user" + from;
        String filename = userDir + "/toUser" + to + ".txt";

        File dir = new File(userDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String line = (isSend ? "我 (" + from + ")" : "用户 (" + from + ")") + ": " + message;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistoryMessages() {
        String storageRoot = "STORAGE";
        String userDir = storageRoot + "/user" + selfId;
        String filename = userDir + "/toUser" + targetId + ".txt";

        File file = new File(filename);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "暂无历史记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            chatArea.setText("");
            String line;
            while ((line = reader.readLine()) != null) {
                chatArea.append(line + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法读取历史记录: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void stopUDPListener() {
        isRunning = false;
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
        if (udpListener != null) {
            try {
                udpListener.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
