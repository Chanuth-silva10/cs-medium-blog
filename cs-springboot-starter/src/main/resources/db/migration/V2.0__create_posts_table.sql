CREATE TABLE posts
(
    id           BIGINT                       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(200)                 NOT NULL,
    content      LONGTEXT                     NULL,
    status       VARCHAR(10) DEFAULT 'DRAFT'  NOT NULL,
    developer_id BIGINT                       NOT NULL,
    created_at   datetime                     NULL,
    updated_at   datetime                     NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);

ALTER TABLE posts ADD CONSTRAINT uc_posts_title UNIQUE (title);

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_on_developer FOREIGN KEY (developer_id) REFERENCES developers (id);
