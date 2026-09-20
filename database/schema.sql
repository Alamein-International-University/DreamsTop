-- ============================================================================
-- DreamsTop (i-Wish) - Database Schema DDL
-- Author: Omar ElSharkawy (@omarehab544)
-- Role: Database Design, DAO Layer & Schema
-- Target Engine: MySQL 8.x / InnoDB
-- Charset: utf8mb4 / utf8mb4_unicode_ci
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `dreamstop_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `dreamstop_db`;

-- Disable foreign key checks during schema recreation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `contributions`;
DROP TABLE IF EXISTS `wishlist_items`;
DROP TABLE IF EXISTS `friendships`;
DROP TABLE IF EXISTS `items`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 1. USERS TABLE
-- Stores user credentials, profile information, and wallet balance.
-- Specs: [1] Register/Sign-in, Profile, Balance Management
-- ============================================================================
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
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `chk_user_balance_positive` CHECK (`balance` >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 2. ITEMS TABLE (Catalog)
-- Stores the global store catalog items from which users pick to build wishlists.
-- Specs: [12] Adding items from where the users can build their wish list
-- ============================================================================
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. FRIENDSHIPS TABLE
-- Manages friend requests, approvals, and friend relationships between users.
-- Specs: [2] Add/Remove Friend, [3] Accept/Decline Request, [5] View Friends
-- ============================================================================
CREATE TABLE `friendships` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `requester_id` INT NOT NULL,
    `addressee_id` INT NOT NULL,
    `status` ENUM('PENDING', 'ACCEPTED', 'DECLINED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_friend_requester` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_friend_addressee` FOREIGN KEY (`addressee_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_no_self_friend` CHECK (`requester_id` <> `addressee_id`),
    CONSTRAINT `uq_friendship_pair` UNIQUE (`requester_id`, `addressee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_friendship_requester` ON `friendships` (`requester_id`);
CREATE INDEX `idx_friendship_addressee` ON `friendships` (`addressee_id`);
CREATE INDEX `idx_friendship_status` ON `friendships` (`status`);

-- ============================================================================
-- 4. WISHLIST_ITEMS TABLE
-- Stores items added to a user's personal wishlist with funding targets and progress.
-- Specs: [4] Create/Update/Delete Wishlist, [6] View Friends Wishlist
-- ============================================================================
CREATE TABLE `wishlist_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `item_id` INT NOT NULL,
    `notes` TEXT,
    `priority` ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    `target_amount` DECIMAL(12, 2) NOT NULL,
    `current_paid_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `is_completed` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_wishlist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_wishlist_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_target_amount_positive` CHECK (`target_amount` > 0.00),
    CONSTRAINT `chk_paid_amount_non_negative` CHECK (`current_paid_amount` >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_wishlist_user` ON `wishlist_items` (`user_id`);
CREATE INDEX `idx_wishlist_item` ON `wishlist_items` (`item_id`);
CREATE INDEX `idx_wishlist_completed` ON `wishlist_items` (`is_completed`);

-- ============================================================================
-- 5. CONTRIBUTIONS TABLE
-- Records monetary contributions from friends toward a specific wishlist item.
-- Specs: [7] Contribute in buying items with specific amount of money
-- ============================================================================
CREATE TABLE `contributions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `contributor_id` INT NOT NULL,
    `wishlist_item_id` INT NOT NULL,
    `amount` DECIMAL(12, 2) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_contrib_user` FOREIGN KEY (`contributor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_contrib_wishlist_item` FOREIGN KEY (`wishlist_item_id`) REFERENCES `wishlist_items` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_contrib_amount_positive` CHECK (`amount` > 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_contrib_contributor` ON `contributions` (`contributor_id`);
CREATE INDEX `idx_contrib_wishlist_item` ON `contributions` (`wishlist_item_id`);

-- ============================================================================
-- 6. NOTIFICATIONS TABLE
-- Stores in-app notifications for friend requests, contributions, and gift completions.
-- Specs: [8] [Buyer] completion notification, [9] [Receiver] gift bought notification
-- ============================================================================
CREATE TABLE `notifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `recipient_id` INT NOT NULL,
    `type` ENUM(
        'FRIEND_REQUEST',
        'FRIEND_REQUEST_ACCEPTED',
        'CONTRIBUTION_RECEIVED',
        'ITEM_COMPLETED_BUYER',
        'ITEM_COMPLETED_RECEIVER'
    ) NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `related_item_id` INT DEFAULT NULL,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notification_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notification_item` FOREIGN KEY (`related_item_id`) REFERENCES `wishlist_items` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_notification_recipient` ON `notifications` (`recipient_id`);
CREATE INDEX `idx_notification_unread` ON `notifications` (`recipient_id`, `is_read`);
