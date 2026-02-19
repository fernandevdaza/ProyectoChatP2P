/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.repository;

import lombok.*;

import java.io.Serializable;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Peer implements Serializable, Model {

    public static final String ME_CODE = "";

    public static final class Column {
        public static final String ID = "id";
        public static final String DISPLAY_NAME = "display_name";
        public static final String IS_SELF = "is_self";
        public static final String LAST_IP_ADDR = "last_ip_addr";
        public static final String LAST_PORT = "last_port";
        public static final String LAST_SEEN_AT = "last_seen_at";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }

    private String id;
    private String displayName;
    private Integer isSelf;
    private String lastIpAddr;
    private Integer lastPort;
    private LocalDateTime lastSeenAt;
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