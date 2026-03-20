package com.fernandev.chatp2p.model.repository;

import com.fernandev.chatp2p.model.entities.db.Peer;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachePeerDao implements IPeerDao{
    private final Map<String, Peer> peers = new ConcurrentHashMap<>();
    private final IPeerDao iPeerDao;

    public CachePeerDao(IPeerDao iPeerDao){
        super();
        this.iPeerDao = iPeerDao;
    }


    @Override
    public List<Peer> findAll() throws ConnectException, SQLException {
        if(peers.isEmpty()){
            List<Peer> peersFromDB = iPeerDao.findAll();
            for (Peer peer : peersFromDB){
                peers.put(peer.getId(), peer);
            }
        }
        return peers.values().stream().toList();
    }

    @Override
    public List<Peer> findAllExceptMe() {
        if(peers.isEmpty()){
            List<Peer> peersFromDB = iPeerDao.findAllExceptMe();
            for (Peer peer : peersFromDB){
                peers.put(peer.getId(), peer);
            }
        }
        return peers.values().stream().toList();
    }

    @Override
    public boolean exist(String argument) throws ConnectException, SQLException {
        return iPeerDao.exist(argument);
    }

    @Override
    public boolean existByName(String name) throws ConnectException, SQLException {
        if(peers.isEmpty()){
            return iPeerDao.existByName(name);
        }
        for (Peer peer : peers.values()){
            if (peer.getDisplayName().equals(name)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Peer findByName(String name) throws ConnectException, SQLException {
        if(peers.isEmpty()){
            return iPeerDao.findByName(name);
        }
        for (Peer peer : peers.values()){
            if (peer.getDisplayName().equals(name)){
                return peer;
            }
        }
        return null;
    }

    @Override
    public Peer findByIp(String ip) {
        if(peers.isEmpty()){
            return iPeerDao.findByIp(ip);
        }
        for (Peer peer : peers.values()){
            if (peer.getLastIpAddr().equals(ip) && peer.getIsSelf() != 1){
                return peer;
            }
        }
        return null;
    }

    @Override
    public Peer findMe() {
        if(peers.isEmpty()){
            return iPeerDao.findMe();
        }
        for (Peer peer : peers.values()){
            if (peer.getIsSelf() == 1){
                return peer;
            }
        }
        Peer me = iPeerDao.findMe();
        if ( me == null) return null;
        peers.put(me.getId(), me);
        return me;
    }

    @Override
    public boolean existById(String id) throws ConnectException, SQLException {
        if(peers.isEmpty()){
            return iPeerDao.existById(id);
        }
        for (Peer peer : peers.values()){
            if (peer.getId().equals(id)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Peer findById(String id) {
        if(peers.isEmpty()){
            return iPeerDao.findById(id);
        }
        for (Peer peer : peers.values()){
            if (peer.getId().equals(id)){
                return peer;
            }
        }
        return null;
    }

    @Override
    public void update(String query) throws Exception {
        iPeerDao.update(query);
        List<Peer> peersFromDB = iPeerDao.findAll();
        for (Peer peer : peersFromDB){
            if(!peers.containsKey(peer.getId())){
                peers.put(peer.getId(), peer);
            }
        }
    }

    @Override
    public void save(Peer peer) {
       try{
           iPeerDao.save(peer);
           peers.put(peer.getId(), peer);
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    @Override
    public void update(Peer peer) {
        try {
            iPeerDao.update(peer);
            for (Peer singlePeer : peers.values()){
                peers.replace(singlePeer.getId(), peer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void update(String query, String conditionWhere) throws Exception {
        iPeerDao.update(query, conditionWhere);
        List<Peer> peersFromDB = iPeerDao.findAll();
        for (Peer peer : peersFromDB){
            if(!peers.containsKey(peer.getId())){
                peers.put(peer.getId(), peer);
            }
        }
    }
}
