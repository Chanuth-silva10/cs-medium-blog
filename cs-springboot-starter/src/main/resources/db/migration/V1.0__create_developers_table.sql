CREATE TABLE developers
(
    id         BIGINT            NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100)      NOT NULL,
    email      VARCHAR(100)      NOT NULL,
    password   VARCHAR(100)      NOT NULL,
    enabled    TINYINT(1) DEFAULT 0 NOT NULL,
    created_at datetime          NULL,
    updated_at datetime          NULL,
    CONSTRAINT pk_developers PRIMARY KEY (id)
);

ALTER TABLE developers ADD CONSTRAINT uc_developers_name UNIQUE (name);
ALTER TABLE developers ADD CONSTRAINT uc_developers_email UNIQUE (email);
