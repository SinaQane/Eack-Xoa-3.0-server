CREATE DATABASE `eack_xoa`;

CREATE TABLE `eack_xoa`.`users`
(
    `id`           LONG         NOT NULL AUTO_INCREMENT,
    `username`     VARCHAR(64)  NOT NULL,
    `password`     VARCHAR(64)  NOT NULL,
    `name`         VARCHAR(64)  NOT NULL,
    `email`        VARCHAR(64)  NOT NULL,
    `phone_number` VARCHAR(16)  NOT NULL,
    `bio`          VARCHAR(256) NOT NULL,
    `birth_date`   DATE         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa`.`profiles`
(
    `id`               LONG     NOT NULL AUTO_INCREMENT,
    `picture`          LONGTEXT NOT NULL,
    `last_seen`        DATE     NOT NULL,
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
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa`.`tweets`
(
    `id`          LONG         NOT NULL AUTO_INCREMENT,
    `owner`       LONG         NOT NULL,
    `upper_tweet` LONG         NOT NULL,
    `picture`     LONGTEXT     NOT NULL,
    `visible`     BOOL         NOT NULL,
    `text`        VARCHAR(256) NOT NULL,
    `tweet_date`  DATE         NOT NULL,
    `comments`    JSON         NOT NULL,
    `upvotes`     JSON         NOT NULL,
    `downvotes`   JSON         NOT NULL,
    `retweets`    JSON         NOT NULL,
    `reports`     INT          NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner`) REFERENCES users (`id`),
    FOREIGN KEY (`upper_tweet`) REFERENCES tweets (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa`.`groups`
(
    `id`      LONG        NOT NULL AUTO_INCREMENT,
    `title`   VARCHAR(64) NOT NULL,
    `members` JSON        NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa`.`chats`
(
    `id`        LONG        NOT NULL AUTO_INCREMENT,
    `chat_name` VARCHAR(64) NOT NULL,
    `group`     BOOL        NOT NULL,
    `users`     JSON        NOT NULL,
    `messages`  JSON        NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa`.`messages`
(
    `id`                LONG         NOT NULL AUTO_INCREMENT,
    `chat_id`           LONG         NOT NULL,
    `owner_id`          LONG         NOT NULL,
    `tweet_id`          LONG         NOT NULL,
    `index`             INT          NOT NULL,
    `text`              VARCHAR(256) NOT NULL,
    `picture`           LONGTEXT     NOT NULL,
    `message_date_unix` LONG         NOT NULL,
    `seen_list`         JSON         NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`chat_id`) REFERENCES chats (`id`),
    FOREIGN KEY (`owner_id`) REFERENCES users (`id`),
    FOREIGN KEY (`tweet_id`) REFERENCES tweets (`id`)
) ENGINE = InnoDB;

CREATE TABLE `eack_xoa`.`notifications`
(
    `id`           LONG         NOT NULL AUTO_INCREMENT,
    `owner`        LONG         NOT NULL,
    `request_from` LONG         NOT NULL,
    `text`         VARCHAR(256) NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner`) REFERENCES users (`id`),
    FOREIGN KEY (`request_from`) REFERENCES users (`id`)
) ENGINE InnoDB;