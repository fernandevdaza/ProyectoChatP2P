package edu.upb.chatupb_v2.repository;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface ResultProcedureReader<T>{
    T getResult(CallableStatement callableStatement) throws SQLException;
}

