package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.server.dao.ItemDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Standard JDBC implementation of {@link ItemDAO}.
 */
public class ItemDAOImpl implements ItemDAO {

    public ItemDAOImpl() {
    }

    public ItemDAOImpl(DatabaseManager db) {
    }

    private static final String SELECT_BASE = "SELECT id, name, description, category, price, image_url FROM items ";

    @Override
    public List<ItemDTO> getAllItems() throws SQLException {
        List<ItemDTO> items = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY id ASC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapItem(rs));
            }
        }
        return items;
    }

    @Override
    public Optional<ItemDTO> findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapItem(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ItemDTO> findByCategory(String category) throws SQLException {
        List<ItemDTO> items = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE category = ? ORDER BY name ASC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public ItemDTO create(ItemDTO item) throws SQLException {
        String sql = "INSERT INTO items (name, description, category, price, image_url) VALUES (?, ?, ?, ?, ?)";
        int id = 0;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getCategory());
            stmt.setBigDecimal(4, item.getPrice());
            stmt.setString(5, item.getImageUrl());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
        }

        return new ItemDTO(id, item.getName(), item.getDescription(), item.getPrice(), item.getImageUrl(), item.getCategory());
    }

    @Override
    public boolean update(ItemDTO item) throws SQLException {
        String sql = "UPDATE items SET name = ?, description = ?, category = ?, price = ?, image_url = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getCategory());
            stmt.setBigDecimal(4, item.getPrice());
            stmt.setString(5, item.getImageUrl());
            stmt.setInt(6, item.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private ItemDTO mapItem(ResultSet rs) throws SQLException {
        return new ItemDTO(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getString("image_url"),
                rs.getString("category")
        );
    }
}
