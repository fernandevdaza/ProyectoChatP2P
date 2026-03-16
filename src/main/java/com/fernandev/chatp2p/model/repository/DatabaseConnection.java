/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fernandev.chatp2p.model.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseConnection {

    private static final DatabaseConnection connection = new DatabaseConnection();
    private static String url = "jdbc:sqlite:./upbot.db";

    private DatabaseConnection(){

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e){
            throw new IllegalStateException("No se encuentra el driver JDBC de SQLite");
        }
    }

    public static void setUrl(String urlConnection){
        url = urlConnection;
    }


    public void initDatabase() {
        String sqlScript = readSqlScript("db/schema.sql");


        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            for (String sql : sqlScript.split(";")) {
                String trimmedSql = sql.trim();

                if (!trimmedSql.isEmpty()) {
                    stmt.execute(trimmedSql);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo inicializar la base de datos", e);
        }
    }

    private String readSqlScript(String resourcePath) {
        InputStream inputStream = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalStateException("No se encontró el script SQL en resources: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder sqlContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sqlContent.append(line).append("\n");
            }

            return sqlContent.toString();

        } catch (IOException e) {
            throw new RuntimeException("Error al leer el script SQL: " + resourcePath, e);
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