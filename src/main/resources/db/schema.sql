CREATE TYPE site_status AS ENUM ('INDEXING', 'INDEXED', 'FAILED');

CREATE TABLE site
(
    id          SERIAL PRIMARY KEY  NOT NULL,
    status      site_status         NOT NULL,
    status_time TIMESTAMP           NOT NULL,
    last_error  TEXT,
    url         VARCHAR(255) UNIQUE NOT NULL,
    name        VARCHAR(255)        NOT NULL
);

CREATE TABLE page
(
    id      SERIAL PRIMARY KEY NOT NULL,
    site_id INT                NOT NULL,
    path    TEXT               NOT NULL,
    code    INT                NOT NULL,
    content TEXT               NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE CASCADE
);

CREATE INDEX page_path ON page (path);
ALTER TABLE page
    ADD CONSTRAINT uk_page_site_path UNIQUE (site_id, path);

CREATE TABLE lemma
(
    id        SERIAL PRIMARY KEY NOT NULL,
    site_id   INT                NOT NULL,
    lemma     VARCHAR(255)       NOT NULL,
    frequency INT                NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE CASCADE
);

ALTER TABLE lemma
    ADD CONSTRAINT uk_lemma_site UNIQUE (lemma, site_id);

CREATE TABLE index
(
    id       SERIAL PRIMARY KEY NOT NULL,
    page_id  INT                NOT NULL,
    lemma_id INT                NOT NULL,
    rank     FLOAT              NOT NULL,
    FOREIGN KEY (page_id) REFERENCES page (id),
    FOREIGN KEY (lemma_id) REFERENCES lemma (id)
);