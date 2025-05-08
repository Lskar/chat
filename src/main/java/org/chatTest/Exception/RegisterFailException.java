package org.chatTest.Exception;

public class RegisterFailException extends RuntimeException {
    public RegisterFailException(String message) {
        super(message);
    }
}
