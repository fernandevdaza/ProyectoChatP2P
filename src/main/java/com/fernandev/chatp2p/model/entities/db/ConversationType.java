package com.fernandev.chatp2p.model.entities.db;

public enum ConversationType {
    DIRECT(1), GROUP(2);

    private final int value;
    ConversationType(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
