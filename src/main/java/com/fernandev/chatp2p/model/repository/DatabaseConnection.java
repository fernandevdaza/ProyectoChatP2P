/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fernandev.chatp2p.model.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {

    private static final DatabaseConnection connection = new DatabaseConnection();
    private final String url;

    private DatabaseConnection(){
        this.url = "jdbc:sqlite:./upbot.db";

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e){
            throw new IllegalStateException("No se encuentra el driver JDBC de SQLite");
        }
    }


    public Connection connect() {

        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e){
            throw new DatabaseException("No se pudo conectar a SQLite: " + url, e);
        }
    }

    public static DatabaseConnection getInstance(){
        return connection;
    }

    public static final class DatabaseException extends RuntimeException {
        public DatabaseException(String message, Throwable cause){
            super(message, cause);
        }
    }
}