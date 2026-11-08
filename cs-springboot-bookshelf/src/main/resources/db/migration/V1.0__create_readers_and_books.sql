CREATE TABLE readers
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    email        VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_readers PRIMARY KEY (id),
    CONSTRAINT uc_readers_email UNIQUE (email)
);

CREATE TABLE books
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    title          VARCHAR(150) NOT NULL,
    author         VARCHAR(100) NOT NULL,
    published_year INT          NOT NULL,
    description    TEXT         NULL,
    CONSTRAINT pk_books PRIMARY KEY (id)
);
