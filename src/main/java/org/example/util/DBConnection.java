package org.example.util;

import org.example.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection instance;

    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    private DBConnection() {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (inputStream == null) {
                throw new DataAccessException("db.properties 파일을 찾을 수 없습니다.");
            }

            properties.load(inputStream);

            this.url = getRequiredProperty(properties, "db.url");
            this.username = getRequiredProperty(properties, "db.username");
            this.password = getRequiredProperty(properties, "db.password");
            this.driver = getRequiredProperty(properties, "db.driver");

            Class.forName(driver);

        } catch (IOException e) {
            throw new DataAccessException("DB 설정 파일을 읽지 못했습니다.", e);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("MariaDB 드라이버를 찾을 수 없습니다.", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }

        return instance;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DataAccessException("DB에 연결하지 못했습니다.", e);
        }
    }

    private String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new DataAccessException("DB 설정값이 비어 있습니다: " + key);
        }

        return value.trim();
    }
}
