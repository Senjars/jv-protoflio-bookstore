package io.github.senjar.bookstoreapp.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySQLContainer extends MySQLContainer<CustomMySQLContainer> {
    private static final String DB_IMAGE = "mysql:8";

    private static CustomMySQLContainer mySQLContainer;

    private CustomMySQLContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomMySQLContainer getInstance() {
        if (mySQLContainer == null) {
            mySQLContainer = new CustomMySQLContainer();
        }
        return mySQLContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("Test_DB_URL", mySQLContainer.getJdbcUrl());
        System.setProperty("Test_DB_USERNAME", mySQLContainer.getUsername());
        System.setProperty("Test_DB_PASSWORD", mySQLContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
