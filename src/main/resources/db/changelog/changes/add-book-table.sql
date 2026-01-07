--liquibase formatted sql
--changeset senjar:1

CREATE TABLE books (
                       id BIGINT AUTO_INCREMENT NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       isbn VARCHAR(255) NOT NULL,
                       price DECIMAL(10, 2) NOT NULL,
                       description VARCHAR(255),
                       cover_image VARCHAR(255),
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       CONSTRAINT PK_BOOKS PRIMARY KEY (id),
                       CONSTRAINT UC_BOOKS_ISBN UNIQUE (isbn)
);