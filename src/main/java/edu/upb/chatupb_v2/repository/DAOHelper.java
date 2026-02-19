package edu.upb.chatupb_v2.repository;

import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Autor      :Ricardo Laredo
 * Date       :21-11-18
 */
public class DAOHelper<T> {
    public DAOHelper(){
    }

    public List<T> executeQuery(String query, ResultReader<T> reader) throws ConnectException, SQLException {
        return executeQuery(query, null, reader);
    }

    public List<T> executeQuery(String query, QueryParameters params, ResultReader<T> reader) throws DatabaseConnection.DatabaseException, SQLException {
        try (Connection conn = DatabaseConnection.getInstance().connect();
             PreparedStatement ps = conn.prepareStatement(query);
        ){


            if (params != null){
                params.setParameters(ps);
            }


            if( ps.execute() ){
                List<T> results = new ArrayList<T>();
                try (ResultSet result = ps.getResultSet()){
                    while(result.next()){
                        T value = reader.getResult(result);
                        if (value != null){
                            results.add(value);
                        }
                    }
                    return results;
                }
            }
            return new ArrayList<>();

        }catch (DatabaseConnection.DatabaseException e){
            System.out.println("No se pudo conectar a la DB: " +  e.getMessage());
            throw e;
        }catch (SQLException e){
            System.out.println("Error SQL en query: " +  e.getMessage());
            throw e;
        }catch (Exception e){
            System.out.println("Error inesperado en query: "+ e.getMessage());
            throw new SQLException(e);
        }
    }

    public void insert(String query, QueryParameters params, Model model) throws Exception{
        try (Connection conn = DatabaseConnection.getInstance().connect();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ){


            if (params != null){
                params.setParameters(ps);
            }


            if( ps.executeUpdate() > 0 ){
                try (ResultSet result = ps.getGeneratedKeys()){
                    if(result.next()){
                        model.setId(String.valueOf(Long.parseLong(result.getString(1))));
                    }
                }
            }

        } catch (SQLException e){
            System.out.println("Error SQL en query: " +  e.getMessage());
            throw new SQLException(e);
        }catch (Exception e){
            System.out.println("Error inesperado en query: "+ e.getMessage());
            throw new SQLException(e);
        }

    }

    public void update(String query, QueryParameters params) throws Exception {
        try (Connection conn = DatabaseConnection.getInstance().connect();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ){


            if (params != null){
                params.setParameters(ps);
            }

            ps.executeUpdate();

        } catch (SQLException e){
            System.out.println("Error SQL en query: " +  e.getMessage());
            throw new SQLException(e);
        }catch (Exception e){
            System.out.println("Error inesperado en query: "+ e.getMessage());
            throw new SQLException(e);
        }
    }

    public int executeQueryCount(String query, QueryParameters params) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().connect();
             PreparedStatement ps = conn.prepareStatement(query);
        ){

            if (params != null){
                params.setParameters(ps);
            }

            if ( ps.execute() ){
                int rowQty = -1;
                try (ResultSet result = ps.getResultSet()){
                    if(result.next()){
                        rowQty = result.getInt(1);
                    }
                    return rowQty;
                }
            }

        } catch (SQLException e){
            System.out.println("Error SQL en query: " +  e.getMessage());
            throw new SQLException(e);
        }catch (Exception e){
            System.out.println("Error inesperado en query: "+ e.getMessage());
            throw new SQLException(e);
        }

        return -1;
    }

    protected T executeProcedureStore(String query, QueryParameters params, ResultProcedureReader<T> reader) throws Exception {
        try (Connection conn = DatabaseConnection.getInstance().connect();
             CallableStatement pc = conn.prepareCall(query);
        ){

            T value = null;

            if (params != null){
                params.setParameters(pc);
            }

            if ( pc.execute() ){
                value = reader.getResult(pc);
            }
            return value;

        } catch (SQLException e){
            System.out.println("Error SQL en query: " +  e.getMessage());
            throw new SQLException(e);
        }catch (Exception e){
            System.out.println("Error inesperado en query: "+ e.getMessage());
            throw new SQLException(e);
        }
    }

}