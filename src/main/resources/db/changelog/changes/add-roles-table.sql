--liquibase formatted sql
--changeset senjar:4

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uc_roles_name UNIQUE (name)
);

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');