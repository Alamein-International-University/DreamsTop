-- ============================================================================
-- DreamsTop (i-Wish) - Turnkey Database Backup & Setup Script
-- Project: i-Wish Desktop Application (Orange Belt - ITI)
-- Team Member: Omar ElSharkawy (@omarehab544)
-- Role: Database Design, DAO Layer & Schema
-- Database: dreamstop_db (MySQL 8.x)
-- Delivery Package: database scheme / backup
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `dreamstop_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `dreamstop_db`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `contributions`;
DROP TABLE IF EXISTS `wishlist_items`;
DROP TABLE IF EXISTS `friendships`;
DROP TABLE IF EXISTS `items`;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- Table structure for `users`
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- Table structure for `items` (Store Catalog)
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- Table structure for `friendships`
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- Table structure for `wishlist_items`
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- Table structure for `contributions`
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- Table structure for `notifications`
-- ----------------------------------------------------------------------------
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

-- ============================================================================
-- DUMPING DATA
-- ============================================================================

-- 1. USERS (Default password: "password123")
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `full_name`, `balance`, `avatar_color`, `bio`) VALUES
(1, 'tarnished693', 'yousef@dreamstop.com',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Yousef Gamal',       50000.00, '#6366F1', 'Client UI / JavaFX Views [2-6]'),
(2, 'kady_x',       'kady@dreamstop.com',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Mohamed ElKady',     75000.00, '#10B981', 'Architecture, Maven Setup, Common Module & Server'),
(3, 'adham_hatem',  'adham@dreamstop.com',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Adham Hatem',        60000.00, '#F59E0B', 'Client UI / JavaFX Views & Controllers [1, 10]'),
(4, 'omarehab544',  'sharkawy@dreamstop.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Omar ElSharkawy',    80000.00, '#EC4899', 'Database Design, DAO Layer & Schema'),
(5, 'ohmarha5554',  'omarhany@dreamstop.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Omar Hany',          65000.00, '#8B5CF6', 'Database Design, Manipulate the Database [11, 12]'),
(6, 'abdullah_s',   'abdullah@dreamstop.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Abdullah Salah',     55000.00, '#3B82F6', 'Handles the clients connections & requests [13, 14]');

-- 2. STORE CATALOG ITEMS
INSERT INTO `items` (`id`, `name`, `description`, `category`, `price`, `image_url`, `icon_emoji`) VALUES
(1,  'NVIDIA GeForce RTX 5090',          'Flagship GPU with 32GB GDDR7, 4nm Blackwell architecture. The absolute king of gaming and AI workloads.', 'GPU',         89000.00, NULL, '🔥'),
(2,  'NVIDIA GeForce RTX 5080',          'High-end Blackwell GPU with 16GB GDDR7, perfect for 4K gaming and ray tracing at max settings.',           'GPU',         45000.00, NULL, '🟢'),
(3,  'AMD Radeon RX 9070 XT',            'AMD RDNA 4 flagship with 16GB GDDR6, excellent 1440p & 4K rasterization and ray tracing performance.',    'GPU',         32000.00, NULL, '🔴'),
(4,  'PlayStation 5 Pro',                 'Enhanced PS5 with 2x faster GPU, 4K@60fps with ray tracing, 2TB SSD storage, includes DualSense.',       'Console',     28000.00, NULL, '🎮'),
(5,  'Xbox Series X',                     'Microsoft''s most powerful console, 12 TFLOPS GPU, 1TB NVMe SSD, 4K/120fps gaming.',                      'Console',     22000.00, NULL, '🟩'),
(6,  'Nintendo Switch 2',                 'Next-gen hybrid handheld/console with 4K docked output, new Joy-Con 2 with magnetic attach, 256GB.',     'Console',     18000.00, NULL, '🔵'),
(7,  'Steam Deck OLED (1TB)',             'Valve''s portable PC gaming device with stunning 7.4" OLED display, Ryzen APU, 1TB NVMe SSD.',          'Handheld',    24000.00, NULL, '🖥️'),
(8,  'Steam Gift Card (500 EGP)',         'Valve Steam digital gift card - spend on any game, DLC, or hardware on the Steam store.',                'Steam',         500.00, NULL, '💳'),
(9,  'Steam Gift Card (1000 EGP)',        'Valve Steam digital gift card - spend on any game, DLC, or hardware on the Steam store.',                'Steam',        1000.00, NULL, '💳'),
(10, 'Razer DeathAdder V3 Pro',           'Wireless esports gaming mouse, 90-hour battery, optical switches, Focus Pro 30K sensor.',               'Peripherals',  4500.00, NULL, '🖱️'),
(11, 'SteelSeries Arctis Nova Pro',       'Wireless gaming headset with ANC, dual-wireless, hot-swap battery, premium 40mm speaker drivers.',      'Peripherals',  9500.00, NULL, '🎧'),
(12, 'Samsung Odyssey G9 (57" OQLED)',   '57" curved ultra-wide OQLED gaming monitor, 240Hz, 0.03ms, FreeSync Premium Pro + G-Sync.',           'Monitor',     85000.00, NULL, '🖥️'),
(13, 'Elden Ring: Shadow of the Erdtree', 'FromSoftware''s legendary open-world soulslike + major DLC. Over 300 hours of content.',                'Game',        1200.00, NULL, '⚔️'),
(14, 'Cyberpunk 2077: Ultimate Edition',  'CD Projekt Red''s open-world RPG + Phantom Liberty DLC. Fully patched and optimized.',                   'Game',        1000.00, NULL, '🌆'),
(15, 'GTA VI',                            'Rockstar''s most ambitious open-world game set in Vice City. Release 2025.',                              'Game',        1400.00, NULL, '🌴'),
(16, 'Helldivers 2',                      'Co-op third-person shooter spreading Managed Democracy across the galaxy.',                               'Game',         800.00, NULL, '🪖'),
(17, 'Black Myth: Wukong',                'Action RPG based on Chinese mythology. Stunning Unreal Engine 5 visuals, 40+ hour campaign.',           'Game',        1100.00, NULL, '🐒'),
(18, 'Indiana Jones & the Great Circle',  'Bethesda/MachineGames first-person adventure game. Best single-player game of 2024.',                   'Game',         950.00, NULL, '🎩');

-- 3. FRIENDSHIPS
INSERT INTO `friendships` (`id`, `requester_id`, `addressee_id`, `status`) VALUES
(1, 1, 2, 'ACCEPTED'),
(2, 1, 3, 'ACCEPTED'),
(3, 1, 4, 'ACCEPTED'),
(4, 2, 3, 'ACCEPTED'),
(5, 2, 5, 'ACCEPTED'),
(6, 6, 1, 'PENDING');

-- 4. WISHLIST ITEMS
INSERT INTO `wishlist_items` (`id`, `user_id`, `item_id`, `notes`, `priority`, `target_amount`, `current_paid_amount`, `is_completed`) VALUES
(1,  1, 1,  'RTX 5090 - MUST HAVE for my new build. Black edition if available!', 'HIGH',   89000.00, 35000.00, FALSE),
(2,  1, 7,  'Steam Deck OLED for on the go gaming between lectures.',               'HIGH',   24000.00, 12000.00, FALSE),
(3,  1, 17, 'Black Myth: Wukong - heard it is stunning on 5090!',                   'MEDIUM',  1100.00,  1100.00, TRUE),
(4,  1, 15, 'GTA VI Day One copy!',                                                 'MEDIUM',  1400.00,   200.00, FALSE),
(5,  2, 12, '57" curved ultrawide for multi-monitor backend debugging setups.',     'HIGH',   85000.00, 42000.00, FALSE),
(6,  2, 18, 'Indiana Jones - best single player game in years.',                    'LOW',     950.00,     0.00, FALSE),
(7,  2, 9,  'Steam card to stock up on games during summer sale.',                  'MEDIUM',  1000.00,   500.00, FALSE),
(8,  3, 4,  'PS5 Pro for exclusive titles. Disc edition please.',                   'HIGH',   28000.00, 14000.00, FALSE),
(9,  3, 13, 'Elden Ring with Shadow of the Erdtree DLC included.',                  'MEDIUM',  1200.00,  1200.00, TRUE),
(10, 3, 10, 'Razer DeathAdder V3 Pro for pixel-perfect UI design work.',             'MEDIUM',  4500.00,  1500.00, FALSE),
(11, 4, 2,  'RTX 5080 for database visualization and GPU-accelerated queries.',     'HIGH',   45000.00, 10000.00, FALSE),
(12, 4, 14, 'Cyberpunk 2077 Ultimate Edition - fully patched!',                     'LOW',    1000.00,     0.00, FALSE),
(13, 5, 6,  'Nintendo Switch 2 for portable gaming anywhere.',                     'HIGH',   18000.00,  9000.00, FALSE),
(14, 5, 16, 'Helldivers 2 with friends for democracy!',                              'MEDIUM',   800.00,   800.00, TRUE),
(15, 6, 5,  'Xbox Series X for Game Pass and Forza.',                               'HIGH',   22000.00,  5000.00, FALSE),
(16, 6, 11, 'SteelSeries Arctis Nova Pro Wireless for late-night coding sessions.', 'MEDIUM',  9500.00,  3000.00, FALSE),
(17, 6, 3,  'RX 9070 XT as a great price-to-performance upgrade.',                  'HIGH',   32000.00,  8000.00, FALSE);

-- 5. CONTRIBUTIONS
INSERT INTO `contributions` (`id`, `contributor_id`, `wishlist_item_id`, `amount`, `created_at`) VALUES
(1, 2, 1, 20000.00, CURRENT_TIMESTAMP),
(2, 4, 1, 15000.00, CURRENT_TIMESTAMP),
(3, 3, 3, 1100.00, CURRENT_TIMESTAMP),
(4, 1, 9, 1200.00, CURRENT_TIMESTAMP),
(5, 1, 5, 22000.00, CURRENT_TIMESTAMP),
(6, 4, 5, 20000.00, CURRENT_TIMESTAMP);

-- 6. NOTIFICATIONS
INSERT INTO `notifications` (`id`, `recipient_id`, `type`, `title`, `message`, `related_item_id`, `is_read`, `created_at`) VALUES
(1, 1, 'FRIEND_REQUEST',         'New Friend Request', 'Abdullah Salah sent you a friend request.', NULL, FALSE, CURRENT_TIMESTAMP),
(2, 1, 'CONTRIBUTION_RECEIVED',  'Contribution Received', 'Omar ElSharkawy contributed 15000.00 EGP towards your RTX 5090!', 1, TRUE, CURRENT_TIMESTAMP),
(3, 1, 'ITEM_COMPLETED_RECEIVER','Gift Goal Reached!', 'Congratulations! Your wishlist item "Black Myth: Wukong" has been fully funded by Adham Hatem!', 3, FALSE, CURRENT_TIMESTAMP),
(4, 3, 'ITEM_COMPLETED_BUYER',   'Gift Completed', 'You completed Yousef Gamal''s wishlist item "Black Myth: Wukong"! It is ready for delivery.', 3, TRUE, CURRENT_TIMESTAMP),
(5, 3, 'ITEM_COMPLETED_RECEIVER','Gift Goal Reached!', 'Congratulations! Your wishlist item "Elden Ring: Shadow of the Erdtree" has been fully funded by Yousef Gamal!', 9, TRUE, CURRENT_TIMESTAMP);
