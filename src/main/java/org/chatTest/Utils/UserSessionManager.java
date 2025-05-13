package org.chatTest.Utils;

import org.chatTest.GUI.ChatWindow;
import org.chatTest.GUI.FriendListFrame;

import java.io.*;
import java.net.*;
import java.util.*;

public class UserSessionManager {
    private static final Map<Integer, UserSession> sessions = new HashMap<>();

    public static void registerUser(int userId, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        sessions.put(userId, new UserSession(userId, socket, in, out));
    }

    public static void addChatWindow(int userId, ChatWindow chatWindow) {
        UserSession session = sessions.get(userId);
        if (session != null) {
            session.addChatWindow(chatWindow);
        }
    }

    public static void logout(int userId) {
        UserSession session = sessions.remove(userId);
        if (session != null) {
            // 关闭所有窗口
            session.closeAllWindows();
            // 关闭 IO 流和 Socket
            session.disconnect();
        }
    }

    private static class UserSession {
        private final int userId;
        private final Socket socket;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private final List<ChatWindow> chatWindows = new ArrayList<>();
        private FriendListFrame friendListFrame;

        public UserSession(int userId, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            this.userId = userId;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        public void setFriendListFrame(FriendListFrame frame) {
            this.friendListFrame = frame;
        }

        public void addChatWindow(ChatWindow window) {
            chatWindows.add(window);
        }

        public void closeAllWindows() {
            for (ChatWindow window : chatWindows) {
                window.dispose();
            }
            chatWindows.clear();

            if (friendListFrame != null) {
                friendListFrame.dispose();
            }
        }

        public void disconnect() {
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        }
    }
}
