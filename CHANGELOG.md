# Changelog 📜

All notable changes to the **DreamsTop (i-Wish)** project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- GitHub issue templates for bug reports (`bug_report.md`) and feature proposals (`feature_request.md`).
- Centralized technical architecture specification in [`ARCHITECTURE.md`](ARCHITECTURE.md) detailing protocol envelopes, database constraints, and threading model.
- Dedicated Visual Tour and Demo showcase placeholders in `docs/screenshots/` and `docs/videos/`.

### Refactored
- Centralized UI model mapping and numeric ID parsing into [`ModelMapper.java`](dreamstop-client/src/main/java/com/dreamstop/util/ModelMapper.java) (`com.dreamstop.util`), eliminating code duplication across client services.

---

## [0.5.0] - 2026-09-20

### Added
- **Glassmorphic JavaFX Styling Overhaul**: Expanded [`styles.css`](dreamstop-client/src/main/resources/com/dreamstop/css/styles.css) (888 lines) with modern dark themes, responsive stat cards, navigation badges, and hover transitions ([#33](https://github.com/Alamein-International-University/DreamsTop/pull/33)).
- **Avatar Utility**: Added [`UiStyleUtil.java`](dreamstop-client/src/main/java/com/dreamstop/util/UiStyleUtil.java) for dynamic user avatar styling and colored backgrounds.

### Removed
- Removed legacy Maven archetype boilerplate files (`PrimaryController`, `SecondaryController`, `primary.fxml`, `secondary.fxml`) ([#33](https://github.com/Alamein-International-University/DreamsTop/pull/33)).

---

## [0.4.0] - 2026-09-20

### Added
- **Real-Time Push Notification Engine**: Built `SessionManager` on the server to dispatch instant push notifications over live sockets to buyers and receivers upon goal milestones ([#30](https://github.com/Alamein-International-University/DreamsTop/pull/30)).
- **Client Network Singleton**: Implemented `NetworkClient` supporting asynchronous request/response dispatch via `CompletableFuture` and real-time push notification listeners ([#29](https://github.com/Alamein-International-University/DreamsTop/pull/29)).
- **Authentication Views**: Built modern Glassmorphic Login and Registration screens with session management, quick-login chips, and local offline fallback ([#31](https://github.com/Alamein-International-University/DreamsTop/pull/31)).
- **Zero-Config Database Fallback**: Added automatic detection in `DatabaseManager` to initialize an embedded in-memory H2 database if MySQL is unavailable ([#31](https://github.com/Alamein-International-University/DreamsTop/pull/31)).

### Fixed
- Fixed embedded H2 runtime driver loading by promoting dependency scope from `test` to default runtime classpath in `dreamstop-server` ([#32](https://github.com/Alamein-International-University/DreamsTop/pull/32)).

---

## [0.3.0] - 2026-09-20

### Changed
- **Network Protocol Refactoring**: Replaced legacy Java Object Serialization with line-delimited UTF-8 JSON framing for cross-platform interoperability and debugging clarity ([#17](https://github.com/Alamein-International-University/DreamsTop/pull/17)).
- **Java 21 Toolchain**: Upgraded project compiler target and runtime configuration to Java 21 across all modules while retaining Java 11 bytecode compatibility in `dreamstop-common` ([#15](https://github.com/Alamein-International-University/DreamsTop/pull/15), [#16](https://github.com/Alamein-International-University/DreamsTop/pull/16)).

### Added
- **Server Admin GUI**: Created `ServerGuiApp` and `ServerGuiController` providing start/stop controls, client connection counters, and real-time server logs ([#16](https://github.com/Alamein-International-University/DreamsTop/pull/16)).
- **CI / Automation Pipeline**: Added GitHub Actions Maven workflow (`maven.yml`) and Dependabot automated dependency management ([#19](https://github.com/Alamein-International-University/DreamsTop/pull/19), [#20](https://github.com/Alamein-International-University/DreamsTop/pull/20), [#22](https://github.com/Alamein-International-University/DreamsTop/pull/22)).

---

## [0.2.0] - 2026-09-17

### Added
- **Database Schema & Relational DDL**: Designed normalized relational schema with constraints in `schema.sql` and store seed data in `seed.sql` ([#13](https://github.com/Alamein-International-University/DreamsTop/pull/13)).
- **Data Access Layer (DAOs)**: Implemented complete JDBC DAO layer (`UserDAO`, `ItemDAO`, `FriendshipDAO`, `WishlistDAO`, `ContributionDAO`, `NotificationDAO`) with automated test suite ([#13](https://github.com/Alamein-International-University/DreamsTop/pull/13)).
- **Server Lifecycle Core**: Implemented `ServerDaemon` and interactive CLI controller supporting configurable ports, graceful shutdowns, and thread pool execution ([#6](https://github.com/Alamein-International-University/DreamsTop/pull/6)).
- **Initial Client Views**: Created baseline JavaFX layouts for wishlist management and friend relationships ([#8](https://github.com/Alamein-International-University/DreamsTop/pull/8)).
- **Common DTO Contracts**: Defined shared domain models and authentication request/response payloads in `dreamstop-common` ([#10](https://github.com/Alamein-International-University/DreamsTop/pull/10)).

---

## [0.1.0] - 2026-09-16

### Added
- **Multi-Module Maven Architecture**: Initialized parent aggregator POM with `dreamstop-common`, `dreamstop-server`, and `dreamstop-client` ([#1](https://github.com/Alamein-International-University/DreamsTop/pull/1)).
- **Project Documentation**: Established initial README, GitHub Pull Request template, and contributing guidelines ([#2](https://github.com/Alamein-International-University/DreamsTop/pull/2), [#3](https://github.com/Alamein-International-University/DreamsTop/pull/3)).
- **Architecture Diagram**: Added architecture conceptual diagram in `explain.tldr`.

---

[Unreleased]: https://github.com/Alamein-International-University/DreamsTop/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/Alamein-International-University/DreamsTop/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/Alamein-International-University/DreamsTop/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/Alamein-International-University/DreamsTop/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Alamein-International-University/DreamsTop/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Alamein-International-University/DreamsTop/releases/tag/v0.1.0
