package com.fernandev.chatp2p.model.entities.command;

import com.fernandev.chatp2p.model.network.SocketClient;

import java.util.regex.Pattern;

public class EnviarContacto extends MessageProtocol {
    public String idUser;
    public String userName;
    public String userIp;

    public EnviarContacto(String idUser, String userName, String userIp) {
        super("020");
        this.idUser = idUser;
        this.userName = userName;
        this.userIp = userIp;
    }

    public EnviarContacto(){
        super("020");
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public static EnviarContacto parse(String message) {
        String[] split = message.split(Pattern.quote("|"));
        if (split.length != 4) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new EnviarContacto(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + getIdUser() + "|" + getUserName() + "|" + getUserIp() + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {

    }

    @Override
    public void onReceive(SocketClient client) {

    }
}
