package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.repository.CachePeerDao;
import com.fernandev.chatp2p.model.repository.IPeerDao;
import com.fernandev.chatp2p.model.repository.PeerDao;
import com.fernandev.chatp2p.view.interfaces.IView;

import java.time.LocalDateTime;
import java.util.List;

public class PeerController {
    private static PeerController peerController = new PeerController();
    private IPeerDao peerDao = new CachePeerDao(new PeerDao());
    ;
    private IView view;

    public PeerController() {
    }

    public void setView(IView view){
        this.view = view;
    }

    public static PeerController getInstance(){
        return peerController;
    }

    public void onLoad() {
        List<Peer> peers = peerDao.findAllExceptMe();
        if (peers != null) {
            System.out.println("[" + Thread.currentThread().getName() + "] Peers encontrados: " + peers.size());
            view.renderPeers(peers);
        } else {
            System.err.println("[" + Thread.currentThread().getName() + "] No se encontraron peers");
        }
    }

    public Peer getMyself() {
        return peerDao.findMe();
    }

    public void savePeer(String ip, String id, String displayName, int port) {
       try {
           if (ip == null && id == null && displayName == null)
               return;
           Peer peer = Peer.builder()
                   .id(id)
                   .displayName(displayName)
                   .isSelf(0)
                   .lastIpAddr(ip)
                   .lastPort(port)
                   .lastSeenAt(LocalDateTime.now())
                   .createdAt(LocalDateTime.now())
                   .updatedAt(LocalDateTime.now())
                   .build();
           peerDao.save(peer);
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public String getPeerIdByIp(String ip) {
        Peer peer = peerDao.findByIp(ip);
        if (peer == null)
            return null;
        return peer.getId();
    }

    public Peer getPeerById(String id){
        Peer peer = peerDao.findById(id);
        return peer;
    }

    public String getPeerIpById(String id) {
        Peer peer = peerDao.findById(id);
        if (peer == null)
            return null;
        return peer.getLastIpAddr();
    }

    public String getPeerDisplayNameById(String id) {
        Peer peer = peerDao.findById(id);
        if (peer != null)
            return peer.getDisplayName();
        return null;
    }

    public String getPeerNameByIp(String ip) {
        Peer peer = peerDao.findByIp(ip);
        if (peer == null)
            return null;
        return peer.getDisplayName();
    }

    public Peer getPeerByIp(String ip){
        Peer peer = peerDao.findByIp(ip);
        if (peer == null){
            return null;
        }
        return peer;
    }

    public void setPeerStatus(String peerId, boolean status){
        Peer peer = peerDao.findById(peerId);
        peer.setConnected(status);
    }
}
