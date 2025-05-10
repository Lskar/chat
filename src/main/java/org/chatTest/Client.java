package org.chatTest;

import org.chatTest.GUI.ChatJFrame;
import org.chatTest.GUI.LoginJFrame;
import org.chatTest.Utils.MessageUtils;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {


    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8880;

    public static void main(String[] args) throws Exception {

        SwingUtilities.invokeLater(() -> new LoginJFrame());

    }
//    public static void connectToServer(String message) {
//        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
//             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
//
//             out.writeObject(message);
//             String response = (String) in.readObject();
//             System.out.println("服务器响应: " + response);
//             if (response.startsWith("LOGIN_SUCCESS")){
//                 System.out.println("登录成功");
//                 int userId = Integer.parseInt(response.split(":")[1]);
//                 new ChatJFrame(userId,in,out);
//             }
//             else{
//                 MessageUtils.showErrorMessage(response);
//             }
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
//    }
//public static void connectToServer(String message) {
//    try {
//        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
//        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//
//        out.writeObject(message);
//        out.flush();
//        String response = (String) in.readObject();
//        System.out.println("服务器响应: " + response);
//
//        if (response.startsWith("LOGIN_SUCCESS")) {
//            int userId = Integer.parseInt(response.split(":")[1]);
//            new ChatJFrame(userId, socket, in, out);
//        } else {
//            MessageUtils.showErrorMessage(response);
//            in.close();
//            out.close();
//            socket.close();
//        }
//    } catch (Exception e) {
//        e.printStackTrace(System.err);
//        MessageUtils.showErrorMessage("连接失败：" + e.getMessage());
//    }
//}

    public static void connectToServer(String message) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(message);
            out.flush(); // ⚠️ 刷新写入缓冲区

            String response = (String) in.readObject();
            System.out.println("【DEBUG】收到服务器响应：" + response);

            if (response != null && response.startsWith("LOGIN_SUCCESS")) {
                int userId = Integer.parseInt(response.split(":")[1]);
                new ChatJFrame(userId, socket, in, out); // 传入所有资源
            } else {
                MessageUtils.showErrorMessage(response == null ? "空响应" : response);
                in.close();
                out.close();
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
            MessageUtils.showErrorMessage("连接失败：" + e.getMessage());
        }
    }
}
