package org.chatTest.Utils;

import org.chatTest.Exception.LoginFailException;
import org.chatTest.Exception.RegisterFailException;
import org.chatTest.Exception.UserExistsException;

import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class SQLUtils {


    private static final String url;
    private static final String username;
    private static final String password;
    private static final Logger log = Logger.getLogger(SQLUtils.class.getName());

    static {
        try {
            Properties prop = new Properties();
            prop.load(new FileReader("src\\main\\resources\\driver.properties"));
            url = prop.getProperty("url");
            username = prop.getProperty("username");
            password = prop.getProperty("password");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void startTransaction(Connection conn) {
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void commitTransaction(Connection conn) {

        try {
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    public static void rollbackTransaction(Connection conn) {
        try {
            if(conn!=null){
                conn.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void registerUser(String username, String password) throws Exception{
        String  sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            startTransaction(conn);
            checkUserExists(conn, username);
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            int rowsAffected = ps.executeUpdate();
            commitTransaction(conn);
            if (rowsAffected==0) {
                throw new RegisterFailException("REGISTER_FAIL");
            }
        }
        catch (Exception e) {
            log.severe("注册失败"+ e.getMessage());
            rollbackTransaction(conn);
            throw e;
        }
        finally {
            close(conn, ps, null);
        }
    }

    private static void checkUserExists(Connection conn,String username) throws Exception{

        String sql = "SELECT * FROM users WHERE username = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                throw new UserExistsException("REGISTER_USER_EXISTS");
            }
        }
    }


    public static void establishFriends(int id1, int id2) throws Exception {
        String checkSql = "SELECT * FROM friends WHERE user_id = ? AND friend_id = ?";
        String insertSql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement checkPs = null;
        PreparedStatement insertPs = null;
        try {
            conn = getConnection();
            checkPs = conn.prepareStatement(checkSql);
            insertPs = conn.prepareStatement(insertSql);
            // 开启事务
            startTransaction(conn);

            // 检查是否已经是好友
            checkPs.setInt(1, id1);
            checkPs.setInt(2, id2);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                throw new RuntimeException("好友关系已存在");
            }

            // 插入双向好友关系
            insertPs.setInt(1, id1);
            insertPs.setInt(2, id2);
            insertPs.executeUpdate();

            insertPs.setInt(1, id2);
            insertPs.setInt(2, id1);
            insertPs.executeUpdate();

            // 提交事务
            commitTransaction(conn);
        } catch (SQLException e) {
            // 回滚事务
            rollbackTransaction(conn);
            throw new RuntimeException("添加好友失败: " + e.getMessage());
        } finally {
            close(conn, checkPs, null);
        }
    }

    public static String[] getFriends(int userId) throws SQLException {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<String> friends = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int friendId = rs.getInt("friend_id");
                friends.add("User" + friendId);
            }
        }
        return friends.toArray(new String[0]);
    }

    public static String[] getFriendsWithStatus(int userId) throws SQLException {
        String sql = "SELECT f.friend_id, u.status FROM friends f JOIN users u ON f.friend_id = u.id WHERE f.user_id = ?";
        List<String> friendsWithStatus = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int friendId = rs.getInt("friend_id");
                String status = rs.getString("status");
                friendsWithStatus.add("User" + friendId + ":" + status.toLowerCase());
            }
        }
        return friendsWithStatus.toArray(new String[0]);
    }

    public static Map<String, Object> loginUser(String username, String password) throws Exception{
        Connection  conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try {
            conn = getConnection();
            ps= conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if(rs.next()){
                result.put("username", rs.getString("username"));
                result.put("password", rs.getString("password"));
                result.put("userId", rs.getInt("id"));
            }
            else {
                throw new LoginFailException("账号或密码错误");
            }
            return result;
        }
        catch (Exception e) {
            log.severe("登录失败"+ e.getMessage());
            throw e;
        }
        finally {
            close(conn, ps, rs);
        }
    }
    public static int changeStatus(int userId, String status){
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try(Connection conn = getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}