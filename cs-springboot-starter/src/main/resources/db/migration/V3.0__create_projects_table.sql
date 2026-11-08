-- The projects table was first created by Hibernate in the earlier chapters.
-- IF NOT EXISTS lets this migration work on a fresh database and on one that already has the table.
CREATE TABLE IF NOT EXISTS projects
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(120) NULL,
    description LONGTEXT     NULL,
    status      VARCHAR(20)  NULL,
    is_public   TINYINT(1)   NOT NULL DEFAULT 0,
    max_members INT          NOT NULL DEFAULT 0,
    created_at  datetime     NOT NULL,
    updated_at  datetime     NOT NULL,
    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT uc_projects_title UNIQUE (title)
);
