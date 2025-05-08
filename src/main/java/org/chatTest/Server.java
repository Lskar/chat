package org.chatTest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws Exception{
        int port = 8880;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器已启动，等待连接...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新客户端连接: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
