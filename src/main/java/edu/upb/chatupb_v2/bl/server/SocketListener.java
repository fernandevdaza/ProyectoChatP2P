package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.MessageProtocol;

public interface SocketListener {
    void onMessage(SocketClient socketClient, MessageProtocol messageProtocol);
}
