package edu.upb.chatupb_v2.repository;

public enum MessageType {
    AUDIO(1), TEXT(2), IMAGE(3), SYSTEM(4);

    private final int value;
    MessageType(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
