package org.chatTest.Utils;

import org.chatTest.Exception.LoginFailException;
import org.chatTest.Exception.RegisterFailException;
import org.chatTest.Exception.UserExistsException;

import java.io.FileReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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

}
