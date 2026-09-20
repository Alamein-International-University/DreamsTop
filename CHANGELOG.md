# Changelog 📜

All notable changes to the **DreamsTop (i-Wish)** project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-09-20

### Added
- **Multi-Module Maven Structure**: Initialized root project with `dreamstop-parent`, `dreamstop-common`, `dreamstop-server`, and `dreamstop-client`.
- **Relational Database Schema**: Added DDL scripts (`schema.sql`, `seed.sql`) with tables for `users`, `items`, `friendships`, `wishlist_items`, `contributions`, and `notifications`.
- **Full DAO Layer**: Implemented thread-safe JDBC DAOs (`UserDAO`, `ItemDAO`, `FriendshipDAO`, `WishlistDAO`, `ContributionDAO`, `NotificationDAO`).
- **Embedded In-Memory Fallback**: Added automatic detection in `DatabaseManager` to switch from MySQL to an in-memory H2 database if MySQL is not running.
- **TCP Socket Server Daemon**: Built `ServerDaemon` and `ClientHandler` supporting line-delimited UTF-8 JSON request/response framing.
- **Server Request Handlers**: Built dedicated dispatchers for `AuthRequestHandler`, `FriendRequestHandler`, `WishlistRequestHandler`, and `ContributionRequestHandler`.
- **Real-Time Push Notifications**: Engineered `SessionManager` to push instant socket alerts to gift owners and contributors upon funding milestones.
- **Modern JavaFX Client GUI**:
  - Glassmorphic Login & Registration view (`login_view.fxml`).
  - Personal Wishlist management with live statistical cards (`wishlist_view.fxml`).
  - Friends Hub with 3-tab navigation (`friends_view.fxml`).
  - Friend Wishlist viewer with real-time contribution modal (`friend_wishlist_view.fxml`).
  - Live Toast Notification overlay for pop-up alerts.
- **Server Admin GUI**: Developed JavaFX dashboard (`ServerGuiApp`) with live client counters, start/stop toggle, and event logs.
- **Automated Test Suite**: Added 24 unit and integration tests covering DAOs, network client, and handlers.

### Changed
- Migrated compiler target to Java 21 across all client and server modules while keeping `dreamstop-common` on Java 11 for maximum bytecode compatibility.
- Switched network communication from legacy Java Object Serialization to standard UTF-8 JSON.

### Fixed
- Fixed runtime `ClassNotFoundException` / `SQLException` by adjusting H2 dependency scope from `test` to default runtime classpath in `dreamstop-server/pom.xml`.
- Fixed JavaFX module exports and reflection bindings across controllers and FXML loaders.

### Refactored
- Created centralized `ModelMapper` utility to eliminate code duplication in DTO-to-Model conversions across client services.
- Cleaned up boilerplate archetype files (`PrimaryController`, `SecondaryController`, `primary.fxml`, `secondary.fxml`).
