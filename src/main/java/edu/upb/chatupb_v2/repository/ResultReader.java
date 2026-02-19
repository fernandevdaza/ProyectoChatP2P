package edu.upb.chatupb_v2.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultReader<T> {
    T getResult(ResultSet result) throws SQLException;
}
