package com.fernandev.chatp2p.model.entities.db;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable, Model {
    private static final long serialVersionUID = 1L;

    public static final class Column {
        public static final String ID = "id";
        public static final String CONVERSATION_ID = "conversation_id";
        public static final String SENDER_PEER_ID = "sender_peer_id";
        public static final String TYPE = "type";
        public static final String TEXT_CONTENT = "text_content";
        public static final String SENT_AT = "sent_at";
        public static final String RECEIVED_AT = "received_at";
        public static final String IS_EPHEMERAL = "is_ephemeral";
        public static final String EXPIRES_AT = "expires_at";
        public static final String STATUS = "status";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }

    @Override
    public void setId(String id){
        this.id = id;
    }

    @Override
    public String getId(){
        return this.id;
    }

    public boolean getIsEphemeral(){
        return this.is_ephemeral;
    }

    private String id;
    private String conversation_id;
    private String sender_peer_id;
    private MessageType type;
    private String text_content;
    //    @Builder.Default
    private LocalDateTime sent_at = LocalDateTime.now();
    private LocalDateTime received_at;
    private boolean is_ephemeral = false;
    private LocalDateTime expires_at;
    private MessageStatusType status = MessageStatusType.RECEIVED;
    //    @Builder.Default
    private LocalDateTime created_at = LocalDateTime.now();
    private LocalDateTime updated_at;
}

