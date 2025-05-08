
### 建表语句

```mysql
CREATE DATABASE chat_app;

USE chat_app;

-- 用户表
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       status ENUM('ONLINE', 'OFFLINE') DEFAULT 'OFFLINE'
);

-- 好友关系表
CREATE TABLE friends (
                         user_id INT NOT NULL,
                         friend_id INT NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES users(id),
                         FOREIGN KEY (friend_id) REFERENCES users(id),
                         PRIMARY KEY (user_id, friend_id)
);

-- 聊天记录表
CREATE TABLE chat_history (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              sender_id INT NOT NULL,
                              receiver_id INT NOT NULL,
                              message TEXT,
                              timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (sender_id) REFERENCES users(id),
                              FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```