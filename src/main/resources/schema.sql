CREATE TABLE MESSAGES (
                          ID                BIGINT AUTO_INCREMENT PRIMARY KEY,
                          MESSAGE           VARCHAR,
                          TOMOBILENUMBER    VARCHAR(255),
                          TYPEOFMSG         VARCHAR(20),
                          MEDIAURL          VARCHAR(255),
                          CAPTION           VARCHAR(2000),
                          FILENAME          VARCHAR(500),
                          SENDSTATUS        CHAR default '0',
                          CREATEDON         TIMESTAMP,
                          CREATEDON_DATE    VARCHAR,
                          SENTON            DATE
--                           UNIQUE (MEDIAURL, MESSAGE, TOMOBILENUMBER, CREATEDON_DATE)
);


