CREATE TABLE categories
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uc_categories_name UNIQUE (name)
);

CREATE TABLE products
(
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(255)   NULL,
    price       DECIMAL(10, 2) NOT NULL,
    available   BOOLEAN        NOT NULL DEFAULT TRUE,
    category_id BIGINT         NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_on_category FOREIGN KEY (category_id) REFERENCES categories (id)
);
