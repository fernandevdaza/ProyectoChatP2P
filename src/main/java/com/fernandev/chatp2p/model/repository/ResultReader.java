package com.fernandev.chatp2p.model.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultReader<T> {
    T getResult(ResultSet result) throws SQLException;
}
