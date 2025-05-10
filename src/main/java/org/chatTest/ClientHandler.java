package org.chatTest;

import org.chatTest.Exception.LoginFailException;
import org.chatTest.Exception.RegisterFailException;
import org.chatTest.Exception.UserExistsException;
import org.chatTest.Utils.SQLUtils;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable{


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
            if(request.startsWith("REGISTER")){
                handleRegister(request);
            }
            if(request.startsWith("LOGIN")){
                handleLogin(request);
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace(System.err);
//            }
        }
    }
    private void handleRegister(String request) throws Exception{
        String[] parts = request.split(":");
        String username = parts[1];
        String password = parts[2];
//        String message = SQLUtils.registerUser(username, password);
//        if(message.startsWith("REGISTER_SUCCESS")){
//            out.writeObject("注册成功");
//        }
//        else if(message.startsWith("REGISTER_FAIL")){
//            out.writeObject("注册失败");
//        }
//        else if(message.startsWith("REGISTER_USER_EXISTS")){
//            out.writeObject("用户名已存在");
//        }
//        out.flush();
        try{
            SQLUtils.registerUser(username, password);
        }
        catch (RegisterFailException e){
            out.writeObject("注册失败");
            return;
        }
        catch (UserExistsException e){
            out.writeObject("用户名已存在");
            return;
        }
        out.writeObject("注册成功");
        out.flush();
    }
    private void handleLogin(String request) throws Exception{
        String[] parts = request.split(":");
        String username = parts[1];
        String password = parts[2];

        try{
            Map<String, Object> result = SQLUtils.loginUser(username, password);
            if(result.containsKey("userId")){
                int userId = (int) result.get("userId");
                out.writeObject("LOGIN_SUCCESS:"+userId);
                out.flush();
            }
        }
        catch (LoginFailException e){
            out.writeObject("登录失败"+e.getMessage());
            out.flush();
        }

    }


}



