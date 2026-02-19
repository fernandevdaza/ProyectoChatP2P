package edu.upb.chatupb_v2.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryParameters {
    void setParameters(PreparedStatement ps) throws SQLException;
}
