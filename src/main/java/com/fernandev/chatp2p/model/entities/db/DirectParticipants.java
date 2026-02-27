package com.fernandev.chatp2p.model.entities.db;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DirectParticipants implements Serializable, Model {

    public static final class Column {
        public static final String CONVERSATION_ID = "conversation_id";
        public static final String PEER_ID = "peer_id";
    }

    private String conversationId;
    private String peerId;

    @Override
    public void setId(String id) {
        this.conversationId = id;
    }

    @Override
    public String getId() {
        return this.conversationId;
    }

}
