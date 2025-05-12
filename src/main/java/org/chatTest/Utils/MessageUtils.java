package org.chatTest.Utils;

import javax.swing.*;

public class MessageUtils extends JFrame {


    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, " " + message, "错误", JOptionPane.ERROR_MESSAGE);
    }


}
