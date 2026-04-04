package com.muse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    private static final String JDBC_URL = "jdbc:mysql://lklklyt.duckdns.org:3306/muse_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "SOUCE*CS102"; // Change to your password
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
            initializeDataSource();
            logger.info("Database connection pool initialized");
        } catch (ClassNotFoundException e) {
            logger.error("MySQL Driver not found", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Data source not initialized");
        }
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
}
