package com.fernandev.chatp2p.model.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryParameters {
    void setParameters(PreparedStatement ps) throws SQLException;
}
