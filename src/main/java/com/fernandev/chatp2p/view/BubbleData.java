package com.fernandev.chatp2p.view;

public class BubbleData {
    public String text;
    public boolean isMe;
    public String messageId;
    public boolean isEphemeral;

    public BubbleData(String text, boolean isMe) {
        this.text = text;
        this.isMe = isMe;
        this.messageId = null;
    }

    public BubbleData(String text, boolean isMe, String messageId, boolean isEphemeral) {
        this.text = text;
        this.isMe = isMe;
        this.messageId = messageId;
        this.isEphemeral = isEphemeral;
    }

    public String getMessageId() {
        return messageId;
    }
    public boolean getIsEphemeral() {
        return isEphemeral;
    }
}
