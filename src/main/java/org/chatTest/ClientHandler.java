package org.chatTest;

import org.chatTest.Exception.LoginFailException;
import org.chatTest.Exception.RegisterFailException;
import org.chatTest.Exception.UserExistsException;
import org.chatTest.Utils.SQLUtils;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            String request = (String) in.readObject();
            if (request.startsWith("REGISTER")) {
                handleRegister(request);
            }
            if (request.startsWith("LOGIN")) {
                handleLogin(request);
            }
            if (request.startsWith("REQUEST_FRIENDS")) {
                handleRequestFriends(request);
            }



            while (true) {
                try {
                    if (in.readObject() instanceof String message) {
                        System.out.println("Received: " + message);///////////
                        if (message.startsWith("MESSAGE")) {
                            //组播发送端代码
                            // 创建MulticastSocket对象
                            MulticastSocket ms = new MulticastSocket();
                            byte[] bytes = message.getBytes();
                            InetAddress address = InetAddress.getByName("255.255.255.255");//这里设置的ip为广播地址
                            int port = 10000;
                            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, address, port);
                            //调用MulticastSocket发送数据方法发送数据
                            ms.send(dp);
                            //释放资源
                            ms.close();
                        } else if (message.startsWith("ADD_FRIEND")) {

                        }else if(message.startsWith("REQUEST_FRIENDS")) {
                            handleRequestFriends(message);
                        }
                    }
                } catch (IOException e) {

                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void handleRegister(String request) throws Exception {
        String[] parts = request.split(":");
        String username = parts[1];
        String password = parts[2];
        try {
            SQLUtils.registerUser(username, password);
        } catch (RegisterFailException e) {
            out.writeObject("注册失败");
            return;
        } catch (UserExistsException e) {
            out.writeObject("用户名已存在");
            return;
        }
        out.writeObject("注册成功");
        out.flush();
    }

    private void handleLogin(String request) throws Exception {
        String[] parts = request.split(":");
        String username = parts[1];
        String password = parts[2];

        try {
            Map<String, Object> result = SQLUtils.loginUser(username, password);
            if (result.containsKey("userId")) {
                int userId = (int) result.get("userId");
                out.writeObject("LOGIN_SUCCESS:" + userId);
                out.flush();
            }
        } catch (LoginFailException e) {
            out.writeObject("登录失败" + e.getMessage());
            out.flush();
        }

    }

    private void handleAddFriends(String request) throws Exception {
        String[] parts = request.split(":");
        String userid = parts[1];
        String friendid = parts[2];
        try {

        } catch (LoginFailException e) {
            out.writeObject("添加失败" + e.getMessage());
            out.flush();
        }

    }
    private void handleRequestFriends(String request) throws Exception {
        int userId = Integer.parseInt(request.split(":")[1]);
        String[] friends = SQLUtils.getFriends(userId);
        StringBuilder sb = new StringBuilder("FRIENDS:");
        for (int i = 0; i < friends.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(friends[i]);
        }
        System.out.println("Send Message:"+sb.toString()+" to user:"+userId);
        out.writeObject(sb.toString());
        out.flush();
    }
}



