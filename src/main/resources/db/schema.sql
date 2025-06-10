CREATE TABLE site
(
    id          INT PRIMARY KEY AUTO_INCREMENT         NOT NULL,
    status      ENUM ('INDEXING', 'INDEXED', 'FAILED') NOT NULL,
    status_time DATETIME                               NOT NULL,
    last_error  TEXT,
    url         VARCHAR(255)                           NOT NULL,
    name        VARCHAR(255)                           NOT NULL
);

CREATE TABLE page
(
    id      INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    site_id INT                            NOT NULL,
    path    TEXT                           NOT NULL,
    code    INT                            NOT NULL,
    content MEDIUMTEXT                     NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE CASCADE
);

CREATE INDEX page_path ON page (path(255));

CREATE TABLE lemma
(
    id        INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    site_id   INT                            NOT NULL,
    lemma     VARCHAR(255)                   NOT NULL,
    frequency INT                            NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE CASCADE
);

CREATE TABLE `index`
(
    id       INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    page_id  INT                            NOT NULL,
    lemma_id INT                            NOT NULL,
    `rank`   FLOAT                          NOT NULL,
    FOREIGN KEY (page_id) REFERENCES page (id),
    FOREIGN KEY (lemma_id) REFERENCES lemma (id)
);