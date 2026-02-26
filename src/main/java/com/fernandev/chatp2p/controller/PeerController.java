package com.fernandev.chatp2p.controller;

import com.fernandev.chatp2p.model.entities.db.Peer;
import com.fernandev.chatp2p.model.repository.PeerDao;
import com.fernandev.chatp2p.view.interfaces.IView;

import java.util.List;

public class PeerController {
    private PeerDao peerDao;
    private IView view;

    public PeerController(IView view){
        peerDao = PeerDao.getInstance();
        this.view = view;
    }

    public void onLoad(){
        List<Peer> peers = peerDao.findAllExceptMe();
        if (peers != null){
            System.out.println("[DB] Peers encontrados: " + peers.size());
            view.onLoad(peers);
        } else{
            System.err.println("[DB] No se encontraron peers");
        }
    }


}
