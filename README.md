# DreamsTop (i-Wish) 🎁

> **ITI - Building Java Desktop Applications (Orange Belt Final Project)**  
> **DreamsTop** is a client-server desktop application inspired by the **i-Wish** specification. It allows users to register, create personal wishlists, connect with friends, view friends' wishlists, and contribute money towards purchasing items for each other.

---

## 📋 Table of Contents
- [DreamsTop (i-Wish) 🎁](#dreamstop-i-wish-)
  - [📋 Table of Contents](#-table-of-contents)
  - [🏗 Project Architecture](#-project-architecture)
  - [🎯 Functional Specifications](#-functional-specifications)
    - [1. Client Application](#1-client-application)
    - [2. Server Application](#2-server-application)
    - [3. Common Module](#3-common-module)
  - [💻 Prerequisites](#-prerequisites)
  - [🚀 Getting Started](#-getting-started)
    - [1. Clone the Repository](#1-clone-the-repository)
    - [2. Build the Entire Project](#2-build-the-entire-project)
    - [3. Run the Client Application](#3-run-the-client-application)
    - [4. Run the Server Application](#4-run-the-server-application)
  - [🤝 Git \& Team Collaboration Guide](#-git--team-collaboration-guide)
  - [👥 Team Members \& Roles](#-team-members--roles)
  - [📄 License](#-license)

---

## 🏗 Project Architecture

DreamsTop uses a **Multi-Module Maven** structure separating concerns between the client interface, shared models/protocols, and server logic:

```
dreamstop/
├── pom.xml                   # Root Parent POM (Manages versions & modules)
├── dreamstop-common/         # Shared models, DTOs, network protocol messages
├── dreamstop-server/         # Backend socket server, database handlers, business logic
└── dreamstop-client/         # JavaFX GUI desktop application (FXML + Controllers)
```

- **`dreamstop-common`**: Shared between client and server. Contains entity models (e.g., `User`, `Item`, `Wishlist`, `Contribution`), network request/response DTOs, and serialization utilities.
- **`dreamstop-server`**: Standalone daemon handling client socket connections on a dedicated port, executing database queries, managing concurrency via threads/thread-pools, and triggering live notifications.
- **`dreamstop-client`**: JavaFX desktop UI allowing users to interact with their wishlists, connect with friends, contribute to gifts, and receive real-time notifications.

---

## 🎯 Functional Specifications

### 1. Client Application
1. **Authentication**: Register new account & Secure Sign-In.
2. **Friend Management**:
   - Search & Add/Remove friends.
   - Send, accept, and decline friend requests.
   - View list of accepted friends.
3. **Wishlist Management**:
   - Create wishlist by adding available items.
   - Update and delete items in personal wishlist.
   - View friends' wishlists with progress bars showing funded status.
4. **Gift Contribution**:
   - Contribute a specific monetary amount towards any item in a friend's wishlist.
   - Live price completion calculation.
5. **Notifications**:
   - **Buyer Notification**: Alerted when a contributed item's price is 100% funded.
   - **Receiver Notification**: Alerted when an item on their wishlist is fully funded, detailing contributing friend(s).
6. **Friendly GUI**: Modern, intuitive JavaFX interface built with FXML and CSS.

### 2. Server Application
1. **Server Lifecycle**: Start / Stop server daemon via admin controls.
2. **Connection Handling**: Manages multiple concurrent client socket connections using multi-threading.
3. **Request Processing**: Parses incoming network requests, interacts with the database, and sends structured responses.
4. **Database Manipulation**:
   - Relational database connectivity (Connection pooling / JDBC).
   - CRUD operations for users, friendships, items, wishlists, and payments.
   - Catalog management (items pool available for users to add to wishlists).

### 3. Common Module
- Network protocol definitions (Action codes, Request/Response payloads).
- Shared domain models and DTOs.
- Validation helpers and constants.

---

## 💻 Prerequisites

Ensure you have the following installed on your machine:
- **Java Development Kit (JDK) 11 or higher** (JDK 17/21 recommended)
  ```bash
  java -version
  ```
- **Apache Maven 3.8+**
  ```bash
  mvn -version
  ```
- **Database Engine** (e.g., MySQL / PostgreSQL / Derby as configured in server)
- **Git**

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/Alamein-International-University/DreamsTop.git
cd DreamsTop
```

### 2. Build the Entire Project
From the repository root directory, run:
```bash
mvn clean compile
```

### 3. Run the Client Application
Launch the JavaFX desktop client:
```bash
mvn -pl dreamstop-client javafx:run
```

### 4. Run the Server Application
Launch the backend server:
```bash
mvn -pl dreamstop-server compile exec:java
```

---

## 🤝 Git & Team Collaboration Guide

> ⚠️ **IMPORTANT**: Direct pushing to the `main` branch is **DISABLED** by repository rulesets. All work must be submitted through **feature branches** and **Pull Requests (PRs)**.

Please read our detailed, beginner-friendly guide:
👉 **[CONTRIBUTING.md](CONTRIBUTING.md)** for:
- Step-by-step branching workflow.
- Standard commit message format.
- How to create and merge Pull Requests.
- How to avoid and solve merge conflicts.

---

## 👥 Team Members & Roles

| Member Name | Role & Responsibilities | GitHub Username |
| :--- | :--- | :--- |
| **Mohamed ElKady** | Architecture, Maven Setup, Common Module & Server | [@kady-x](https://github.com/kady-x) |
| **Adham Hatem** | Client UI / JavaFX Views & Controllers | [@Adham-Hatem](https://github.com/Adham-Hatem) |
| **Omar ElSharkawy** | to be selected | [@omarehab544](https://github.com/omarehab544) |
| **Omar Hany** | to be selected | [@ohmarha5554-spec](https://github.com/ohmarha5554-spec) |
| **Yousef Gamal** | to be selected | [@tarnished693-max](https://github.com/tarnished693-max) |
| **Abdullah Salah** | to be selected | [@AbdullahSalah3](https://github.com/AbdullahSalah3) |

---

## 📄 License
This project is developed for educational purposes under the ITI Java Desktop Applications Track and licensed under the [MIT License](LICENSE).