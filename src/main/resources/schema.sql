CREATE TABLE MESSAGES
(
    ID       INT AUTO_INCREMENT  PRIMARY KEY,
    MESSAGE     VARCHAR(4000),
    TOMOBILENUMBER VARCHAR(15),
    TYPEOFMSG   VARCHAR(20),
    MEDIAURL    VARCHAR(500),
    CAPTION     VARCHAR(2000),
    FILENAME    VARCHAR(500),
    SENDSTATUS     CHAR default '0',
    CREATEDON      DATE,
    SENTON         DATE
);