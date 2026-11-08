CREATE TABLE roles
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(20)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at  datetime(6)  NOT NULL,
    updated_at  datetime(6)  NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uc_roles_name UNIQUE (name)
);

CREATE TABLE users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    role_id    BIGINT       NOT NULL,
    created_at datetime(6)  NOT NULL,
    updated_at datetime(6)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uc_users_email UNIQUE (email),
    CONSTRAINT fk_users_on_role FOREIGN KEY (role_id) REFERENCES roles (id)
);
