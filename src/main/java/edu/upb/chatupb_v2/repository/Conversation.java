package edu.upb.chatupb_v2.repository;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Conversation implements Serializable, Model {

    public static final class Column {
        public static final String ID = "id";
        public static final String TYPE = "type";
        public static final String TITLE = "title";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }

    private String id;
    private ConversationType type;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

}
