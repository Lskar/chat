package org.chatTest;

import org.chatTest.GUI.LoginJFrame;
import org.chatTest.Utils.MessageUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// 修改 connectToServer 方法为非静态方法（或返回包含流的对象）
public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8880;

    public static ConnectionResponse connectToServer(String message) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(message);
            out.flush();

            String response = (String) in.readObject();
            System.out.println("【DEBUG】收到服务器响应：" + response);

            if (response != null && response.startsWith("LOGIN_SUCCESS")) {
                int userId = Integer.parseInt(response.split(":")[1]);
                return new ConnectionResponse(userId, socket, in, out);
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
        return null;
    }

    // 内部类用于封装连接结果
    public static class ConnectionResponse {
        public final int userId;
        public final Socket socket;
        public final ObjectInputStream in;
        public final ObjectOutputStream out;

        public ConnectionResponse(int userId, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            this.userId = userId;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }
    }
    public static void main(String[] args) {
        new LoginJFrame();
    }
}
