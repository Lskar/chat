package org.chatTest.GUI;

import org.chatTest.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginJFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginJFrame() {
        setTitle("登录 / 注册");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setResizable(false);

        // 设置面板与布局
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 内边距
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题标签
        JLabel titleLabel = new JLabel("欢迎登录", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // 用户名标签与输入框
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(userLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // 密码标签与输入框
        JLabel passLabel = new JLabel("密   码:");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        JButton loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginButton.setBackground(new Color(0x4A90E2));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                Client.connectToServer("LOGIN:" + username + ":" + password);
            }
        });

        JButton registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerButton.setBackground(new Color(0x50C878));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                Client.connectToServer("REGISTER:" + username + ":" + password);
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // 添加面板到窗口
        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginJFrame());
    }
}
