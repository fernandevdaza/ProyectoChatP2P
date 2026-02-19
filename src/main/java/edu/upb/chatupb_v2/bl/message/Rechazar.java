package edu.upb.chatupb_v2.bl.message;

public class Rechazar extends Message {

    public Rechazar(){
        super("003");
    }

    public static Rechazar parse(String message) {
        String[] split = message.split("\\|");
        if (split.length != 1) throw new IllegalArgumentException("Formato de mensaje no válido");
        return new Rechazar();
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + System.lineSeparator();
    }
}
