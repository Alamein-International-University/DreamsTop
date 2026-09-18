-- ============================================================================
-- DreamsTop (i-Wish) - Database Schema DDL (Server Resource)
-- Author: Omar ElSharkawy (@omarehab544)
-- ============================================================================

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `contributions`;
DROP TABLE IF EXISTS `wishlist_items`;
DROP TABLE IF EXISTS `friendships`;
DROP TABLE IF EXISTS `items`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `balance` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `avatar_color` VARCHAR(20) DEFAULT '#6366F1',
    `bio` VARCHAR(255) DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `chk_user_balance_positive` CHECK (`balance` >= 0.00)
);

CREATE TABLE `items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `category` VARCHAR(50) NOT NULL,
    `price` DECIMAL(12, 2) NOT NULL,
    `image_url` VARCHAR(255) DEFAULT NULL,
    `icon_emoji` VARCHAR(10) DEFAULT '🎁',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `chk_item_price_positive` CHECK (`price` > 0.00)
);

CREATE TABLE `friendships` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `requester_id` INT NOT NULL,
    `addressee_id` INT NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_friend_requester` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_friend_addressee` FOREIGN KEY (`addressee_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_no_self_friend` CHECK (`requester_id` <> `addressee_id`),
    CONSTRAINT `uq_friendship_pair` UNIQUE (`requester_id`, `addressee_id`)
);

CREATE TABLE `wishlist_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `item_id` INT NOT NULL,
    `notes` TEXT,
    `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    `target_amount` DECIMAL(12, 2) NOT NULL,
    `current_paid_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `is_completed` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_wishlist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_wishlist_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_target_amount_positive` CHECK (`target_amount` > 0.00),
    CONSTRAINT `chk_paid_amount_non_negative` CHECK (`current_paid_amount` >= 0.00)
);

CREATE TABLE `contributions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `contributor_id` INT NOT NULL,
    `wishlist_item_id` INT NOT NULL,
    `amount` DECIMAL(12, 2) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_contrib_user` FOREIGN KEY (`contributor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_contrib_wishlist_item` FOREIGN KEY (`wishlist_item_id`) REFERENCES `wishlist_items` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_contrib_amount_positive` CHECK (`amount` > 0.00)
);

CREATE TABLE `notifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `recipient_id` INT NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `related_item_id` INT DEFAULT NULL,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notification_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notification_item` FOREIGN KEY (`related_item_id`) REFERENCES `wishlist_items` (`id`) ON DELETE SET NULL
);
