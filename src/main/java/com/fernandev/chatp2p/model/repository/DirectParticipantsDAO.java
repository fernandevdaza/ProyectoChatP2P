package com.fernandev.chatp2p.model.repository;

import com.fernandev.chatp2p.model.entities.db.DirectParticipants;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DirectParticipantsDAO {
    private DAOHelper<DirectParticipants> helper;
    private final static DirectParticipantsDAO directParticipantsDAO = new DirectParticipantsDAO();

    public static DirectParticipantsDAO getInstance(){
        return directParticipantsDAO;
    }

    private DirectParticipantsDAO(){
        this.helper = new DAOHelper<DirectParticipants>();
    }

    ResultReader<DirectParticipants> resultReader = result ->{
        DirectParticipants directParticipants = new DirectParticipants();
        if (existColumn(result, DirectParticipants.Column.CONVERSATION_ID)) {
            directParticipants.setId(result.getString(DirectParticipants.Column.CONVERSATION_ID));
        }
        if (existColumn(result, DirectParticipants.Column.PEER_ID)) {
            directParticipants.setPeer_id(result.getString(DirectParticipants.Column.PEER_ID));
        }
        return directParticipants;
    };

    public static boolean existColumn(ResultSet result, String columnName){
        try{
            result.findColumn(columnName);
            return true;
        }catch (SQLException e){
            System.out.println("No se encontró la columna: " + e.getMessage());
        }
        return false;
    }

    public List<DirectParticipants> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM direct_participants";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM direct_participants WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public List<DirectParticipants> findDirectParticipantsByConversationId(String conversationId) throws ConnectException, SQLException {
        String query = "SELECT * FROM direct_participants WHERE conversation_id ='" + conversationId + "'";
        System.out.println(query);
        List<DirectParticipants> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }

    public DirectParticipants findConversationByPeerId(String id) {
        try {
            String query = "SELECT * FROM direct_participants WHERE peer_id ='" + id + "'";
            System.out.println(query);
            List<DirectParticipants> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list.getFirst();
        } catch (ConnectException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DirectParticipants> findConversationsByPeerId(String peerId) throws ConnectException, SQLException {
        String query = "SELECT * FROM direct_participants WHERE peer_id ='" + peerId + "'";
        System.out.println(query);
        List<DirectParticipants> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }


    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(DirectParticipants directParticipants) throws Exception {
        String query = "INSERT INTO direct_participants(conversation_id, peer_id) values (?,?)";
        QueryParameters params = new QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, directParticipants.getConversation_id());
                pst.setString(2, directParticipants.getPeer_id());
            }
        };
        helper.insert(query, params, directParticipants);
    }

//    public void update(Peer peer) throws Exception {
//        String query = "UPDATE message SET last_ip_addr=? WHERE id=?";
//        QueryParameters params = new QueryParameters() {
//            @Override
//            public void setParameters(PreparedStatement pst) throws SQLException {
//                pst.setString(1, peer.getLastIpAddr());
//                pst.setString(2, peer.getId());
//            }
//        };
//        helper.update(query, params);
//    }

    public void update(String query, String conditionWhere) throws Exception {
        if (query.trim().endsWith("%s")) {
            query = String.format(query, conditionWhere);
        } else {
            query = String.format("%s %s", query, conditionWhere);
        }
        helper.update(query, null);
    }
}
