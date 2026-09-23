package com.dreamstop.common.protocol;

import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.common.dto.UserDTO;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    @Test
    @DisplayName("JsonUtils: Serialize and deserialize UserDTO")
    void testUserRoundtrip() {
        UserDTO user = new UserDTO(1, "kady_x", "kady@dreamstop.com", new BigDecimal("75000.00"), "Mohamed ElKady", "#6366F1", "Lead Dev");

        String json = JsonUtils.toJson(user);
        assertNotNull(json);
        assertTrue(json.contains("kady_x"));

        UserDTO result = JsonUtils.fromJson(json, UserDTO.class);
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getBalance(), result.getBalance());
        assertEquals(user.getFullName(), result.getFullName());
    }

    @Test
    @DisplayName("JsonUtils: Null and blank strings return null safely")
    void testNullAndBlankSafety() {
        assertNull(JsonUtils.toJson(null));
        assertNull(JsonUtils.fromJson(null, UserDTO.class));
        assertNull(JsonUtils.fromJson("", UserDTO.class));
        assertNull(JsonUtils.fromJson("   ", UserDTO.class));
    }

    @Test
    @DisplayName("JsonUtils: Generic collections serialization and deserialization")
    void testListRoundtrip() {
        List<ItemDTO> items = Arrays.asList(
                new ItemDTO(1, "RTX 5090", "GPU", new BigDecimal("89000.00"), null, "Hardware"),
                new ItemDTO(2, "PS5 Pro", "Console", new BigDecimal("30000.00"), null, "Gaming")
        );

        String json = JsonUtils.toJson(items);
        assertNotNull(json);

        Type listType = new TypeToken<List<ItemDTO>>() {}.getType();
        List<ItemDTO> result = JsonUtils.fromJson(json, listType);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("RTX 5090", result.get(0).getName());
        assertEquals("PS5 Pro", result.get(1).getName());
    }
}
