--liquibase formatted sql
--changeset senjar:5

CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_users_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    COnstraint fk_role_id FOREIGN KEY (role_id) REFERENCES roles (id)
)