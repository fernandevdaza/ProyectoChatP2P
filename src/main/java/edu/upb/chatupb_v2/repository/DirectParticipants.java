package edu.upb.chatupb_v2.repository;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    private String conversation_id;
    private String peer_id;

    @Override
    public void setId(String id) {
        this.conversation_id = id;
    }

    @Override
    public String getId() {
        return this.conversation_id;
    }

}
