package com.fernandev.chatp2p.model.entities.command;

public abstract class MessageProtocol {
    private String codigo;
    private String ip;

    public MessageProtocol(String codigo) {
        this.codigo = codigo;
    }
    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getIp(){
        return ip;
    }
    public void setIp(String ip){
        this.ip = ip;
    }


    public abstract String generarTrama();
}
