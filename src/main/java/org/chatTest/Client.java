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
    public static void connectToServer(String message) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

             out.writeObject(message);
             String response = (String) in.readObject();
             System.out.println("服务器响应: " + response);
             if (response.startsWith("LOGIN_SUCCESS")){
                 int userId = Integer.parseInt(response.split(":")[1]);
                 new ChatJFrame(userId);
             }
             else{
                 MessageUtils.showErrorMessage(response);
             }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
