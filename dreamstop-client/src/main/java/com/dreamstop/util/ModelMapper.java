package com.dreamstop.util;

import com.dreamstop.common.dto.FriendshipDTO;
import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.dto.WishlistItemDTO;
import com.dreamstop.common.model.FriendshipStatus;
import com.dreamstop.model.FriendRequest;
import com.dreamstop.model.Item;
import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;

import java.time.LocalDateTime;

public final class ModelMapper {

    private ModelMapper() {
    }

    public static User toUser(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return new User(
                String.valueOf(dto.getId()),
                dto.getUsername(),
                dto.getUsername(),
                dto.getEmail(),
                "#6366F1",
                "Connected Player"
        );
    }

    public static Item toItem(ItemDTO dto) {
        if (dto == null) {
            return null;
        }
        String emoji = "🎮";
        if (dto.getCategory() != null) {
            switch (dto.getCategory().toUpperCase()) {
                case "GPU" -> emoji = "🔥";
                case "CONSOLE" -> emoji = "🎮";
                case "STEAM" -> emoji = "💳";
                case "PERIPHERALS" -> emoji = "🎧";
                case "MONITOR" -> emoji = "🖥️";
                case "GAME" -> emoji = "⚔️";
                default -> emoji = "🎁";
            }
        }
        return new Item(
                String.valueOf(dto.getId()),
                dto.getName(),
                dto.getDescription() != null ? dto.getDescription() : "",
                dto.getCategory() != null ? dto.getCategory() : "Store",
                dto.getPrice() != null ? dto.getPrice().doubleValue() : 0.0,
                emoji
        );
    }

    public static WishlistItem toWishlistItem(WishlistItemDTO dto) {
        if (dto == null) {
            return null;
        }
        Item itemModel = toItem(dto.getItem());
        double target = itemModel != null ? itemModel.getPrice() : 0.0;
        double current = dto.getCurrentPaidAmount() != null ? dto.getCurrentPaidAmount().doubleValue() : 0.0;

        return new WishlistItem(
                String.valueOf(dto.getId()),
                String.valueOf(dto.getUserId()),
                itemModel,
                "",
                target,
                current,
                "HIGH"
        );
    }

    public static FriendRequest toFriendRequest(FriendshipDTO dto) {
        if (dto == null) {
            return null;
        }
        User sender = toUser(dto.getRequester());
        User receiver = toUser(dto.getAddressee());
        FriendRequest.Status status = FriendRequest.Status.PENDING;
        if (dto.getStatus() == FriendshipStatus.ACCEPTED) {
            status = FriendRequest.Status.ACCEPTED;
        } else if (dto.getStatus() == FriendshipStatus.DECLINED) {
            status = FriendRequest.Status.DECLINED;
        }

        return new FriendRequest(
                String.valueOf(dto.getId()),
                sender,
                receiver,
                status,
                LocalDateTime.now()
        );
    }

    public static int parseNumericId(String idStr) {
        if (idStr == null) {
            return 0;
        }
        if (idStr.startsWith("itm-") || idStr.startsWith("wl-") || idStr.startsWith("usr-") || idStr.startsWith("req-")) {
            idStr = idStr.substring(4);
        }
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
