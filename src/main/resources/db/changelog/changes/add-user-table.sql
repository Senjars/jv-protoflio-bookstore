--liquibase formatted sql
--changeset senjar:2

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE (email)
    )