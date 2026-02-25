package edu.upb.chatupb_v2.repository;


import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDateTime;


public class PeerDao {
    private final DAOHelper<Peer> helper;
    private final static PeerDao peerDao = new PeerDao();

    public static PeerDao getInstance(){
        return peerDao;
    }
    private PeerDao() {
        helper = new DAOHelper<>();
    }

    ResultReader<Peer> resultReader = result -> {
        Peer peer = new Peer();
        if (existColumn(result, Peer.Column.ID)) {
            peer.setId(result.getString(Peer.Column.ID));
        }
        if (existColumn(result, Peer.Column.DISPLAY_NAME)) {
            peer.setDisplayName(result.getString(Peer.Column.DISPLAY_NAME));
        }
        if (existColumn(result, Peer.Column.IS_SELF)) {
            peer.setIsSelf(result.getInt(Peer.Column.IS_SELF));
        }
        if (existColumn(result, Peer.Column.LAST_IP_ADDR)) {
            peer.setLastIpAddr(result.getString(Peer.Column.LAST_IP_ADDR));
        }
        if (existColumn(result, Peer.Column.LAST_PORT)) {
            peer.setLastPort(result.getInt(Peer.Column.LAST_PORT));
        }
        if (existColumn(result, Peer.Column.LAST_SEEN_AT)) {
            peer.setLastSeenAt(LocalDateTime.parse(result.getString(Peer.Column.LAST_SEEN_AT)));
        }
        if (existColumn(result, Peer.Column.CREATED_AT)) {
            peer.setCreatedAt(LocalDateTime.parse(result.getString(Peer.Column.CREATED_AT)));
        }
        if (existColumn(result, Peer.Column.UPDATED_AT)) {
            peer.setUpdatedAt(LocalDateTime.parse(result.getString(Peer.Column.UPDATED_AT)));
        }
        return peer;
    };

    public static boolean existColumn(ResultSet result, String columnName) {
        try {
            result.findColumn(columnName);
            return true;
        } catch (SQLException sqlex) {
//            System.out.println("No se encontro la columna: %s".formatted(columnName));
        }
        return false;
    }

    public List<Peer> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM peers";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM peers WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existByName(String name) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM peers WHERE display_name='" + name + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public Peer findByName(String name) throws ConnectException, SQLException {
        String query = "SELECT * FROM peers WHERE display_name ='" + name + "'";
        System.out.println(query);
        List<Peer> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    public Peer findByIp(String ip){
        try {
            String query = "SELECT * FROM peers WHERE last_ip_addr ='" + ip + "'" + " AND is_self = 0";
            System.out.println(query);
            List<Peer> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list.getFirst();
        }catch (ConnectException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public Peer findMe() {
        try {
            String query = "SELECT * FROM peers WHERE is_self=1";
            System.out.println(query);
            List<Peer> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list.getFirst();
        }catch (ConnectException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean existById(String id) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM peers WHERE id='" + id + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public Peer findById(String id)  {
        try {
            String query = "SELECT * FROM peers WHERE id ='" + id + "'";
            System.out.println(query);
            List<Peer> list = helper.executeQuery(query, resultReader);
            if (list.isEmpty()) {
                return null;
            }
            return list.getFirst();
        }catch (ConnectException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(Peer peer) throws Exception {
        String query = "INSERT INTO peers(id, display_name, is_self, last_ip_addr, last_port, last_seen_at, created_at, updated_at) values (?,?,?,?,?,?,?,?)";
        QueryParameters params = new QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, peer.getId());
                pst.setString(2, peer.getDisplayName());
                pst.setString(3, peer.getIsSelf().toString());
                pst.setString(4, peer.getLastIpAddr());
                pst.setString(5, peer.getLastPort().toString());
                pst.setString(6, peer.getLastSeenAt().toString());
                pst.setString(7, peer.getCreatedAt().toString());
                pst.setString(8, peer.getUpdatedAt().toString());
            }
        };
        helper.insert(query, params, peer);
    }

    public void update(Peer peer) throws Exception {
        String query = "UPDATE contact SET last_ip_addr=? WHERE id=?";
        QueryParameters params = new QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, peer.getLastIpAddr());
                pst.setString(2, peer.getId());
            }
        };
        helper.update(query, params);
    }

    public void update(String query, String conditionWhere) throws Exception {
        if (query.trim().endsWith("%s")) {
            query = String.format(query, conditionWhere);
        } else {
            query = String.format("%s %s", query, conditionWhere);
        }
        helper.update(query, null);
    }
}