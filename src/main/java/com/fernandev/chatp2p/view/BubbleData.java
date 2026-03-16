package com.fernandev.chatp2p.view;

public class BubbleData {
    public String text;
    public boolean isMe;
    public String messageId;

    public BubbleData(String text, boolean isMe) {
        this.text = text;
        this.isMe = isMe;
        this.messageId = null;
    }

    public BubbleData(String text, boolean isMe, String messageId) {
        this.text = text;
        this.isMe = isMe;
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
