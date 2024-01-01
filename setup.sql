CREATE TABLE `message_broadcast`
(
    `id`         INT           NOT NULL,
    `content`    VARCHAR(1000) NOT NULL,
    `author`     VARCHAR(16)   NOT NULL,
    `createdAt`  FLOAT         NOT NULL,
    `type`       VARCHAR(100)  NOT NULL,
    `permission` VARCHAR(100),
    PRIMARY KEY (`id`)
);