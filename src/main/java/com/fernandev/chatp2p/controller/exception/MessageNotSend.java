package com.fernandev.chatp2p.controller.exception;

public class MessageNotSend extends RuntimeException {
    public MessageNotSend(String message) {
        super(message);
    }
}
