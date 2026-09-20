-- ============================================================================
-- DreamsTop (i-Wish) - Initial Seed Data
-- Author: Omar ElSharkawy (@omarehab544)
-- Role: Database Design, DAO Layer & Schema
-- Description: Pre-populates the database with team members, full catalog,
--              initial friendships, wishlist items, contributions, and notifications.
-- ============================================================================

USE `dreamstop_db`;

-- ============================================================================
-- 1. SEED USERS (DreamsTop Team Members)
-- Passwords hashed using SHA-256 (Default password for all demo users: "password123")
-- SHA-256("password123") = ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- ============================================================================
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `full_name`, `balance`, `avatar_color`, `bio`) VALUES
(1, 'tarnished693', 'yousef@dreamstop.com',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Yousef Gamal',       50000.00, '#6366F1', 'Client UI / JavaFX Views [2-6]'),
(2, 'kady_x',       'kady@dreamstop.com',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Mohamed ElKady',     75000.00, '#10B981', 'Architecture, Maven Setup, Common Module & Server'),
(3, 'adham_hatem',  'adham@dreamstop.com',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Adham Hatem',        60000.00, '#F59E0B', 'Client UI / JavaFX Views & Controllers [1, 10]'),
(4, 'omarehab544',  'sharkawy@dreamstop.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Omar ElSharkawy',    80000.00, '#EC4899', 'Database Design, DAO Layer & Schema'),
(5, 'ohmarha5554',  'omarhany@dreamstop.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Omar Hany',          65000.00, '#8B5CF6', 'Database Design, Manipulate the Database [11, 12]'),
(6, 'abdullah_s',   'abdullah@dreamstop.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Abdullah Salah',     55000.00, '#3B82F6', 'Handles the clients connections & requests [13, 14]');

-- ============================================================================
-- 2. SEED ITEMS (Store Catalog - GPUs, Consoles, Games, Peripherals)
-- ============================================================================
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

-- ============================================================================
-- 3. SEED FRIENDSHIPS & REQUESTS
-- Yousef (1) is friends with Kady (2), Adham (3), Sharkawy (4).
-- Kady (2) is friends with Adham (3) and Omar Hany (5).
-- Abdullah (6) sent a pending request to Yousef (1).
-- ============================================================================
INSERT INTO `friendships` (`id`, `requester_id`, `addressee_id`, `status`) VALUES
(1, 1, 2, 'ACCEPTED'),
(2, 1, 3, 'ACCEPTED'),
(3, 1, 4, 'ACCEPTED'),
(4, 2, 3, 'ACCEPTED'),
(5, 2, 5, 'ACCEPTED'),
(6, 6, 1, 'PENDING');

-- ============================================================================
-- 4. SEED WISHLIST ITEMS
-- ============================================================================
INSERT INTO `wishlist_items` (`id`, `user_id`, `item_id`, `notes`, `priority`, `target_amount`, `current_paid_amount`, `is_completed`) VALUES
-- Yousef Gamal (User 1)
(1,  1, 1,  'RTX 5090 - MUST HAVE for my new build. Black edition if available!', 'HIGH',   89000.00, 35000.00, FALSE),
(2,  1, 7,  'Steam Deck OLED for on the go gaming between lectures.',               'HIGH',   24000.00, 12000.00, FALSE),
(3,  1, 17, 'Black Myth: Wukong - heard it is stunning on 5090!',                   'MEDIUM',  1100.00,  1100.00, TRUE),
(4,  1, 15, 'GTA VI Day One copy!',                                                 'MEDIUM',  1400.00,   200.00, FALSE),

-- Mohamed ElKady (User 2)
(5,  2, 12, '57" curved ultrawide for multi-monitor backend debugging setups.',     'HIGH',   85000.00, 42000.00, FALSE),
(6,  2, 18, 'Indiana Jones - best single player game in years.',                    'LOW',     950.00,     0.00, FALSE),
(7,  2, 9,  'Steam card to stock up on games during summer sale.',                  'MEDIUM',  1000.00,   500.00, FALSE),

-- Adham Hatem (User 3)
(8,  3, 4,  'PS5 Pro for exclusive titles. Disc edition please.',                   'HIGH',   28000.00, 14000.00, FALSE),
(9,  3, 13, 'Elden Ring with Shadow of the Erdtree DLC included.',                  'MEDIUM',  1200.00,  1200.00, TRUE),
(10, 3, 10, 'Razer DeathAdder V3 Pro for pixel-perfect UI design work.',             'MEDIUM',  4500.00,  1500.00, FALSE),

-- Omar ElSharkawy (User 4)
(11, 4, 2,  'RTX 5080 for database visualization and GPU-accelerated queries.',     'HIGH',   45000.00, 10000.00, FALSE),
(12, 4, 14, 'Cyberpunk 2077 Ultimate Edition - fully patched!',                     'LOW',    1000.00,     0.00, FALSE),

-- Omar Hany (User 5)
(13, 5, 6,  'Nintendo Switch 2 for portable gaming anywhere.',                     'HIGH',   18000.00,  9000.00, FALSE),
(14, 5, 16, 'Helldivers 2 with friends for democracy!',                              'MEDIUM',   800.00,   800.00, TRUE),

-- Abdullah Salah (User 6)
(15, 6, 5,  'Xbox Series X for Game Pass and Forza.',                               'HIGH',   22000.00,  5000.00, FALSE),
(16, 6, 11, 'SteelSeries Arctis Nova Pro Wireless for late-night coding sessions.', 'MEDIUM',  9500.00,  3000.00, FALSE),
(17, 6, 3,  'RX 9070 XT as a great price-to-performance upgrade.',                  'HIGH',   32000.00,  8000.00, FALSE);

-- ============================================================================
-- 5. SEED CONTRIBUTIONS
-- ============================================================================
INSERT INTO `contributions` (`id`, `contributor_id`, `wishlist_item_id`, `amount`, `created_at`) VALUES
-- Contributions to Yousef's RTX 5090 (item 1, total 35000)
(1, 2, 1, 20000.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 4, 1, 15000.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Contribution completing Yousef's Black Myth: Wukong (item 3, total 1100)
(3, 3, 3, 1100.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Contribution completing Adham's Elden Ring (item 9, total 1200)
(4, 1, 9, 1200.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- Contribution to Kady's monitor (item 5, total 42000)
(5, 1, 5, 22000.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 4, 5, 20000.00, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ============================================================================
-- 6. SEED NOTIFICATIONS
-- ============================================================================
INSERT INTO `notifications` (`id`, `recipient_id`, `type`, `title`, `message`, `related_item_id`, `is_read`, `created_at`) VALUES
(1, 1, 'FRIEND_REQUEST',         'New Friend Request', 'Abdullah Salah sent you a friend request.', NULL, FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 1, 'CONTRIBUTION_RECEIVED',  'Contribution Received', 'Omar ElSharkawy contributed 15000.00 EGP towards your RTX 5090!', 1, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 1, 'ITEM_COMPLETED_RECEIVER','Gift Goal Reached!', 'Congratulations! Your wishlist item "Black Myth: Wukong" has been fully funded by Adham Hatem!', 3, FALSE, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 3, 'ITEM_COMPLETED_BUYER',   'Gift Completed', 'You completed Yousef Gamal''s wishlist item "Black Myth: Wukong"! It is ready for delivery.', 3, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 3, 'ITEM_COMPLETED_RECEIVER','Gift Goal Reached!', 'Congratulations! Your wishlist item "Elden Ring: Shadow of the Erdtree" has been fully funded by Yousef Gamal!', 9, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY));
