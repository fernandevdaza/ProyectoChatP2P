package edu.upb.chatupb_v2.repository;


import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ConversationDao {
    private final DAOHelper<Conversation> helper;
    private final static ConversationDao conversationDao = new ConversationDao();


    public static ConversationDao getInstance(){
        return conversationDao;
    }

    public ConversationDao() {
        helper = new DAOHelper<>();
    }

    ResultReader<Conversation> resultReader = result -> {
        Conversation conversation = new Conversation();
        if (existColumn(result, Conversation.Column.ID)) {
            conversation.setId(result.getString(Conversation.Column.ID));
        }
        if (existColumn(result, Conversation.Column.TYPE)) {
            conversation.setType(ConversationType.valueOf(result.getString(Conversation.Column.TYPE)));
        }
        if (existColumn(result, Conversation.Column.TITLE)) {
            conversation.setTitle(result.getString(Conversation.Column.TITLE));
        }
        if (existColumn(result, Conversation.Column.CREATED_AT)) {
            conversation.setCreatedAt(result.getTimestamp(Conversation.Column.CREATED_AT).toLocalDateTime());
        }
        if (existColumn(result, Conversation.Column.UPDATED_AT)) {
            conversation.setUpdatedAt(result.getTimestamp(Conversation.Column.UPDATED_AT).toLocalDateTime());
        }
        return conversation;
    };

    public static boolean existColumn (ResultSet result, String columnName){
        try {
            result.findColumn(columnName);
            return true;
        } catch (SQLException sqlex) {
//            System.out.println("No se encontro la columna: %s".formatted(columnName));
        }
        return false;
    }

    public List<Conversation> findAll () throws ConnectException, SQLException {
        String query = "SELECT * FROM conversations";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM conversations WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existById (String id) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM conversations WHERE id='" + id + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public Conversation findById (String id) throws ConnectException, SQLException {
        String query = "SELECT * FROM conversations WHERE id ='" + id + "'";
        System.out.println(query);
        List<Conversation> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    public void update (String query) throws Exception {
        helper.update(query, null);
    }

    public void save (Conversation conversation) throws Exception {
        String query = "INSERT INTO conversations(id, type, title, created_at, updated_at) values (?,?,?,?,?)";
        QueryParameters params = new QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, conversation.getId());
                pst.setString(2, conversation.getType().toString());
                pst.setString(3, conversation.getTitle());
                pst.setString(4, conversation.getCreatedAt().toString());
                pst.setString(5, conversation.getUpdatedAt().toString());
            }
        };
        helper.insert(query, params, conversation);
    }

    public void update (String query, String conditionWhere) throws Exception {
        if (query.trim().endsWith("%s")) {
            query = String.format(query, conditionWhere);
        } else {
            query = String.format("%s %s", query, conditionWhere);
        }
        helper.update(query, null);
    }
}
