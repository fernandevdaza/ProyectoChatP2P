package com.fernandev.chatp2p.model.repository;

import com.fernandev.chatp2p.model.entities.db.Peer;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface IPeerDao {
    public List<Peer> findAll() throws ConnectException, SQLException;
    public List<Peer> findAllExceptMe();
    public boolean exist(String argument) throws ConnectException, SQLException;
    public boolean existByName(String name) throws ConnectException, SQLException;
    public Peer findByName(String name) throws ConnectException, SQLException;
    public Peer findByIp(String ip);
    public Peer findMe();
    public boolean existById(String id) throws ConnectException, SQLException;
    public Peer findById(String id);
    public void update(String query) throws Exception;
    public void save(Peer peer) throws Exception;
    public void update(Peer peer) throws Exception;
    public void update(String query, String conditionWhere) throws Exception;
    }
