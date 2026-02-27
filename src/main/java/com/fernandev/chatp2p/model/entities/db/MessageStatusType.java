package com.fernandev.chatp2p.model.entities.db;

public enum MessageStatusType {

    PENDING(0), SENT(1), DELIVERED(2), READ(3), RECEIVED(4), FAILED(5);

    private final int value;
    MessageStatusType(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
