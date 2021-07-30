DROP DATABASE `eack_xoa_server`;

CREATE DATABASE `eack_xoa_server`;

CREATE TABLE `eack_xoa_server`.`users`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `username`     VARCHAR(64)  NOT NULL,
    `password`     VARCHAR(64)  NOT NULL,
    `name`         VARCHAR(64)  NOT NULL,
    `email`        VARCHAR(64)  NOT NULL,
    `phone_number` VARCHAR(16)  NOT NULL,
    `bio`          VARCHAR(256) NOT NULL,
    `birth_date`   DATETIME     NOT NULL DEFAULT '1000-01-01 00:00:00',
    `is_active`    BOOL         NOT NULL,
    `is_deleted`   BOOL         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa_server`.`profiles`
(
    `id`               BIGINT   NOT NULL AUTO_INCREMENT,
    `picture`          LONGTEXT NOT NULL,
    `last_seen`        DATETIME NOT NULL DEFAULT '1000-01-01 00:00:00',
    `followers`        JSON     NOT NULL,
    `followings`       JSON     NOT NULL,
    `blocked`          JSON     NOT NULL,
    `muted`            JSON     NOT NULL,
    `requests`         JSON     NOT NULL,
    `pending`          JSON     NOT NULL,
    `user_tweets`      JSON     NOT NULL,
    `retweeted_tweets` JSON     NOT NULL,
    `upvoted_tweets`   JSON     NOT NULL,
    `downvoted_tweets` JSON     NOT NULL,
    `reported_tweets`  JSON     NOT NULL,
    `saved_tweets`     JSON     NOT NULL,
    `notifications`    JSON     NOT NULL,
    `groups`           JSON     NOT NULL,
    `chats`            JSON     NOT NULL,
    `private_state`    BOOL     NOT NULL,
    `info_state`       BOOL     NOT NULL,
    `last_seen_state`  INT      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa_server`.`tweets`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `owner`       BIGINT       NOT NULL,
    `upper_tweet` BIGINT       NOT NULL,
    `picture`     LONGTEXT     NOT NULL,
    `visible`     BOOL         NOT NULL,
    `text`        VARCHAR(256) NOT NULL,
    `tweet_date`  DATETIME     NOT NULL DEFAULT '1000-01-01 00:00:00',
    `comments`    JSON         NOT NULL,
    `upvotes`     JSON         NOT NULL,
    `downvotes`   JSON         NOT NULL,
    `retweets`    JSON         NOT NULL,
    `reports`     INT          NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner`) REFERENCES users (`id`),
    FOREIGN KEY (`upper_tweet`) REFERENCES tweets (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa_server`.`groups`
(
    `id`      BIGINT      NOT NULL AUTO_INCREMENT,
    `title`   VARCHAR(64) NOT NULL,
    `members` JSON        NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa_server`.`chats`
(
    `id`        BIGINT      NOT NULL AUTO_INCREMENT,
    `chat_name` VARCHAR(64) NOT NULL,
    `group`     BOOL        NOT NULL,
    `users`     JSON        NOT NULL,
    `messages`  JSON        NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa_server`.`messages`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `chat_id`           BIGINT       NOT NULL,
    `owner_id`          BIGINT       NOT NULL,
    `tweet_id`          BIGINT       NOT NULL,
    `index`             INT          NOT NULL,
    `text`              VARCHAR(256) NOT NULL,
    `picture`           LONGTEXT     NOT NULL,
    `message_date_unix` BIGINT       NOT NULL,
    `seen_list`         JSON         NOT NULL,
    `sent`              BOOL         NOT NULL,
    `received`          BOOL         NOT NULL,
    `seen`              BOOL         NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`chat_id`) REFERENCES chats (`id`),
    FOREIGN KEY (`owner_id`) REFERENCES users (`id`),
    FOREIGN KEY (`tweet_id`) REFERENCES tweets (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa_server`.`notifications`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `owner`        BIGINT       NOT NULL,
    `request_from` BIGINT       NOT NULL,
    `text`         VARCHAR(256) NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner`) REFERENCES users (`id`),
    FOREIGN KEY (`request_from`) REFERENCES users (`id`)
) ENGINE InnoDB;