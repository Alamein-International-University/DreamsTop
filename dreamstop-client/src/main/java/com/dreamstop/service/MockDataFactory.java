package com.dreamstop.service;

import com.dreamstop.model.FriendRequest;
import com.dreamstop.model.Item;
import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;

import java.time.LocalDateTime;
import java.util.*;

public class MockDataFactory {

    private static User currentUser;
    private static List<User> allUsers = new ArrayList<>();
    private static List<Item> catalogItems = new ArrayList<>();
    private static Map<String, List<WishlistItem>> wishlistsByUser = new HashMap<>();
    private static List<FriendRequest> friendRequests = new ArrayList<>();
    private static Map<String, Set<String>> friendships = new HashMap<>();

    static {
        initData();
    }

    private static void initData() {
        // =====================
        // 1. TEAM MEMBERS ONLY
        // =====================
        currentUser = new User("usr-1", "tarnished693", "Yousef Gamal",       "yousef@dreamstop.com",    "#6366F1", "Client UI / JavaFX Views [2-6]");
        User kady    = new User("usr-2", "kady_x",      "Mohamed ElKady",     "kady@dreamstop.com",      "#10B981", "Architecture, Maven Setup, Common Module & Server");
        User adham   = new User("usr-3", "adham_hatem", "Adham Hatem",        "adham@dreamstop.com",     "#F59E0B", "Client UI / JavaFX Views & Controllers [1, 10]");
        User sharkawy= new User("usr-4", "omarehab544", "Omar ElSharkawy",    "sharkawy@dreamstop.com",  "#EC4899", "Database Design, DAO Layer & Schema");
        User omarHany= new User("usr-5", "ohmarha5554", "Omar Hany",          "omarhany@dreamstop.com",  "#8B5CF6", "Database Design, Manipulate the Database [11, 12]");
        User abdullah= new User("usr-6", "abdullah_s",  "Abdullah Salah",     "abdullah@dreamstop.com",  "#3B82F6", "Handles the clients connections & requests [13, 14]");

        allUsers.addAll(Arrays.asList(currentUser, kady, adham, sharkawy, omarHany, abdullah));

        // =====================
        // 2. GAMING CATALOG
        // =====================
        // --- GPUs & Hardware ---
        catalogItems.add(new Item("itm-1",  "NVIDIA GeForce RTX 5090",          "Flagship GPU with 32GB GDDR7, 4nm Blackwell architecture. The absolute king of gaming and AI workloads.", "GPU",         89000.0, "🔥"));
        catalogItems.add(new Item("itm-2",  "NVIDIA GeForce RTX 5080",          "High-end Blackwell GPU with 16GB GDDR7, perfect for 4K gaming and ray tracing at max settings.",           "GPU",         45000.0, "🟢"));
        catalogItems.add(new Item("itm-3",  "AMD Radeon RX 9070 XT",            "AMD RDNA 4 flagship with 16GB GDDR6, excellent 1440p & 4K rasterization and ray tracing performance.",    "GPU",         32000.0, "🔴"));

        // --- Consoles ---
        catalogItems.add(new Item("itm-4",  "PlayStation 5 Pro",                 "Enhanced PS5 with 2x faster GPU, 4K@60fps with ray tracing, 2TB SSD storage, includes DualSense.",       "Console",     28000.0, "🎮"));
        catalogItems.add(new Item("itm-5",  "Xbox Series X",                     "Microsoft's most powerful console, 12 TFLOPS GPU, 1TB NVMe SSD, 4K/120fps gaming.",                      "Console",     22000.0, "🟩"));
        catalogItems.add(new Item("itm-6",  "Nintendo Switch 2",                 "Next-gen hybrid handheld/console with 4K docked output, new Joy-Con 2 with magnetic attach, 256GB.",     "Console",     18000.0, "🔵"));

        // --- Steam & PC Games ---
        catalogItems.add(new Item("itm-7",  "Steam Deck OLED (1TB)",             "Valve's portable PC gaming device with stunning 7.4\" OLED display, Ryzen APU, 1TB NVMe SSD.",          "Handheld",    24000.0, "🖥️"));
        catalogItems.add(new Item("itm-8",  "Steam Gift Card (500 EGP)",         "Valve Steam digital gift card — spend on any game, DLC, or hardware on the Steam store.",                "Steam",        500.0, "💳"));
        catalogItems.add(new Item("itm-9",  "Steam Gift Card (1000 EGP)",        "Valve Steam digital gift card — spend on any game, DLC, or hardware on the Steam store.",                "Steam",       1000.0, "💳"));

        // --- Peripherals ---
        catalogItems.add(new Item("itm-10", "Razer DeathAdder V3 Pro",           "Wireless esports gaming mouse, 90-hour battery, optical switches, Focus Pro 30K sensor.",               "Peripherals", 4500.0, "🖱️"));
        catalogItems.add(new Item("itm-11", "SteelSeries Arctis Nova Pro",       "Wireless gaming headset with ANC, dual-wireless, hot-swap battery, premium 40mm speaker drivers.",      "Peripherals", 9500.0, "🎧"));
        catalogItems.add(new Item("itm-12", "Samsung Odyssey G9 (57\" OQLED)",   "57\" curved ultra-wide OQLED gaming monitor, 240Hz, 0.03ms, FreeSync Premium Pro + G-Sync.",           "Monitor",     85000.0, "🖥️"));

        // --- Game Titles (Steam) ---
        catalogItems.add(new Item("itm-13", "Elden Ring: Shadow of the Erdtree", "FromSoftware's legendary open-world soulslike + major DLC. Over 300 hours of content.",                "Game",        1200.0, "⚔️"));
        catalogItems.add(new Item("itm-14", "Cyberpunk 2077: Ultimate Edition",  "CD Projekt Red's open-world RPG + Phantom Liberty DLC. Fully patched and optimized.",                   "Game",        1000.0, "🌆"));
        catalogItems.add(new Item("itm-15", "GTA VI",                            "Rockstar's most ambitious open-world game set in Vice City. Release 2025.",                              "Game",        1400.0, "🌴"));
        catalogItems.add(new Item("itm-16", "Helldivers 2",                      "Co-op third-person shooter spreading Managed Democracy across the galaxy.",                               "Game",         800.0, "🪖"));
        catalogItems.add(new Item("itm-17", "Black Myth: Wukong",                "Action RPG based on Chinese mythology. Stunning Unreal Engine 5 visuals, 40+ hour campaign.",           "Game",        1100.0, "🐒"));
        catalogItems.add(new Item("itm-18", "Indiana Jones & the Great Circle",  "Bethesda/MachineGames first-person adventure game. Best single-player game of 2024.",                   "Game",         950.0, "🎩"));

        // =====================
        // 3. FRIENDSHIPS (all team members friends with each other for demo)
        // =====================
        addFriendship(currentUser.getId(), kady.getId());
        addFriendship(currentUser.getId(), adham.getId());
        addFriendship(currentUser.getId(), sharkawy.getId());
        addFriendship(kady.getId(), adham.getId());
        addFriendship(kady.getId(), omarHany.getId());

        // =====================
        // 4. FRIEND REQUESTS
        // =====================
        // Incoming to Yousef:
        friendRequests.add(new FriendRequest("req-1", abdullah, currentUser, FriendRequest.Status.PENDING, LocalDateTime.now().minusHours(2)));
        // Outgoing from Yousef:
        friendRequests.add(new FriendRequest("req-2", currentUser, adham, FriendRequest.Status.ACCEPTED, LocalDateTime.now().minusDays(2)));

        // =====================
        // 5. WISHLISTS
        // =====================

        // --- Yousef Gamal's Wishlist (RTX 5090 + Games) ---
        List<WishlistItem> yousefWishlist = new ArrayList<>();
        yousefWishlist.add(new WishlistItem("wl-1",  currentUser.getId(), catalogItems.get(0),  "RTX 5090 - MUST HAVE for my new build. Black edition if available!", 89000.0, 35000.0, "HIGH"));
        yousefWishlist.add(new WishlistItem("wl-2",  currentUser.getId(), catalogItems.get(6),  "Steam Deck OLED for on the go gaming between lectures.",               24000.0, 12000.0, "HIGH"));
        yousefWishlist.add(new WishlistItem("wl-3",  currentUser.getId(), catalogItems.get(16), "Black Myth: Wukong — heard it's stunning on 5090!",                    1100.0,  1100.0, "MEDIUM"));
        yousefWishlist.add(new WishlistItem("wl-4",  currentUser.getId(), catalogItems.get(14), "GTA VI Day One copy!",                                                 1400.0,   200.0, "MEDIUM"));
        wishlistsByUser.put(currentUser.getId(), yousefWishlist);

        // --- Mohamed ElKady's Wishlist ---
        List<WishlistItem> kadyWishlist = new ArrayList<>();
        kadyWishlist.add(new WishlistItem("wl-5",  kady.getId(), catalogItems.get(11), "57\" curved ultrawide for multi-monitor backend debugging setups.",  85000.0, 42000.0, "HIGH"));
        kadyWishlist.add(new WishlistItem("wl-6",  kady.getId(), catalogItems.get(17), "Indiana Jones — best single player game in years.",                    950.0,    0.0,  "LOW"));
        kadyWishlist.add(new WishlistItem("wl-7",  kady.getId(), catalogItems.get(8),  "Steam card to stock up on games during summer sale.",                 1000.0,  500.0,  "MEDIUM"));
        wishlistsByUser.put(kady.getId(), kadyWishlist);

        // --- Adham Hatem's Wishlist ---
        List<WishlistItem> adhamWishlist = new ArrayList<>();
        adhamWishlist.add(new WishlistItem("wl-8",  adham.getId(), catalogItems.get(3),  "PS5 Pro for exclusive titles. Disc edition please.",                 28000.0, 14000.0, "HIGH"));
        adhamWishlist.add(new WishlistItem("wl-9",  adham.getId(), catalogItems.get(12), "Elden Ring with Shadow of the Erdtree DLC included.",               1200.0,  1200.0,  "MEDIUM"));
        adhamWishlist.add(new WishlistItem("wl-10", adham.getId(), catalogItems.get(9),  "Razer DeathAdder V3 Pro for pixel-perfect UI design work.",          4500.0,  1500.0,  "MEDIUM"));
        wishlistsByUser.put(adham.getId(), adhamWishlist);

        // --- Omar ElSharkawy's Wishlist ---
        List<WishlistItem> sharkawyWishlist = new ArrayList<>();
        sharkawyWishlist.add(new WishlistItem("wl-11", sharkawy.getId(), catalogItems.get(1), "RTX 5080 for database visualization and GPU-accelerated queries.",    45000.0, 10000.0, "HIGH"));
        sharkawyWishlist.add(new WishlistItem("wl-12", sharkawy.getId(), catalogItems.get(13), "Cyberpunk 2077 Ultimate Edition — finally patched!",                1000.0,    0.0,  "LOW"));
        wishlistsByUser.put(sharkawy.getId(), sharkawyWishlist);

        // --- Omar Hany's Wishlist ---
        List<WishlistItem> omarHanyWishlist = new ArrayList<>();
        omarHanyWishlist.add(new WishlistItem("wl-13", omarHany.getId(), catalogItems.get(5),  "Nintendo Switch 2 for portable gaming anywhere.",                  18000.0,  9000.0, "HIGH"));
        omarHanyWishlist.add(new WishlistItem("wl-14", omarHany.getId(), catalogItems.get(15), "Helldivers 2 with friends for democracy!",                           800.0,   800.0, "MEDIUM"));
        wishlistsByUser.put(omarHany.getId(), omarHanyWishlist);

        // --- Abdullah Salah's Wishlist ---
        List<WishlistItem> abdullahWishlist = new ArrayList<>();
        abdullahWishlist.add(new WishlistItem("wl-15", abdullah.getId(), catalogItems.get(4),  "Xbox Series X for Game Pass and Forza.",                            22000.0,  5000.0, "HIGH"));
        abdullahWishlist.add(new WishlistItem("wl-16", abdullah.getId(), catalogItems.get(10), "SteelSeries Arctis Nova Pro Wireless for late-night coding sessions.", 9500.0, 3000.0, "MEDIUM"));
        abdullahWishlist.add(new WishlistItem("wl-17", abdullah.getId(), catalogItems.get(2),  "RX 9070 XT as a great price-to-performance upgrade.",               32000.0,  8000.0, "HIGH"));
        wishlistsByUser.put(abdullah.getId(), abdullahWishlist);
    }

    private static void addFriendship(String id1, String id2) {
        friendships.computeIfAbsent(id1, k -> new HashSet<>()).add(id2);
        friendships.computeIfAbsent(id2, k -> new HashSet<>()).add(id1);
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        if (user != null) {
            currentUser = user;
            if (!allUsers.contains(user)) {
                allUsers.add(user);
            }
        }
    }

    public static List<User> getAllUsers() {
        return allUsers;
    }

    public static List<Item> getCatalogItems() {
        return catalogItems;
    }

    public static Map<String, List<WishlistItem>> getWishlistsByUser() {
        return wishlistsByUser;
    }

    public static List<FriendRequest> getFriendRequests() {
        return friendRequests;
    }

    public static Map<String, Set<String>> getFriendships() {
        return friendships;
    }
}
