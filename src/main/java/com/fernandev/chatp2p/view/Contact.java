package com.fernandev.chatp2p.view;

public class Contact {
    String id;
    String ip;
    String name;
    boolean isOnline;

    public Contact(String id, String ip, String name, boolean isOnline){
        this.id = id;
        this.ip = ip;
        this.name = name;
        this.isOnline = isOnline;

    }

    public String getIp() {
        return ip;
    }

    public boolean getOnline(){
        return this.isOnline;
    }

    public void setOnline(boolean isOnline){
        this.isOnline = isOnline;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
