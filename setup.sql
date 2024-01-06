CREATE TABLE `message_broadcast`
(
    `uuid`       VARCHAR(36)   NOT NULL,
    `sortId`     INT           NOT NULL,
    `content`    VARCHAR(1000) NOT NULL,
    `author`     VARCHAR(16)   NOT NULL,
    `createdAt`  FLOAT         NOT NULL,
    `permission` VARCHAR(100),
    PRIMARY KEY (`uuid`)
);