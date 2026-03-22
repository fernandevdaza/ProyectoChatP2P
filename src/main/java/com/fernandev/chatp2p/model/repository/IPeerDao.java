package com.fernandev.chatp2p.model.repository;

import com.fernandev.chatp2p.model.entities.db.Peer;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface IPeerDao {
    List<Peer> findAll() throws ConnectException, SQLException;
    List<Peer> findAllExceptMe();
    boolean exist(String argument) throws ConnectException, SQLException;
    boolean existByName(String name) throws ConnectException, SQLException;
    Peer findByName(String name) throws ConnectException, SQLException;
    Peer findByIp(String ip);
    Peer findMe();
    boolean existById(String id) throws ConnectException, SQLException;
    Peer findById(String id);
    void update(String query) throws Exception;
    void save(Peer peer);
    void update(Peer peer);
    void update(String query, String conditionWhere) throws Exception;
    }
