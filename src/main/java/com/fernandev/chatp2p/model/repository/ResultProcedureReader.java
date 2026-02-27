package com.fernandev.chatp2p.model.repository;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface ResultProcedureReader<T>{
    T getResult(CallableStatement callableStatement) throws SQLException;
}

