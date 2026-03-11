package com.fernandev.chatp2p.model.entities.db;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageReceipt implements Serializable, Model {
    private static final long serialVersionUID = 1L;

    public static final class Column {
        public static final String MESSAGE_ID = "message_id";
        public static final String PEER_ID = "peer_id";
        public static final String STATUS = "status";
        public static final String AT_TIME = "at_time";
    }

    private String messageId;
    private String peerId;
    private String status;
    private LocalDateTime atTime;

    @Override
    public void setId(String id) {
        this.messageId = id;
    }

    @Override
    public String getId() {
        return this.messageId;
    }
}
