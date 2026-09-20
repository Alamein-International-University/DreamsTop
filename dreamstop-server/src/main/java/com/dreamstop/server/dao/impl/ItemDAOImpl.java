package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.server.dao.ItemDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concise, clean JDBC implementation of {@link ItemDAO}.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class ItemDAOImpl implements ItemDAO {

    private final DatabaseManager db;

    public ItemDAOImpl() {
        this(DatabaseManager.getInstance());
    }

    public ItemDAOImpl(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_BASE = "SELECT id, name, description, category, price, image_url FROM items ";

    @Override
    public List<ItemDTO> getAllItems() throws SQLException {
        return db.queryList(SELECT_BASE + "ORDER BY id ASC", this::mapItem);
    }

    @Override
    public Optional<ItemDTO> findById(int id) throws SQLException {
        return db.queryOne(SELECT_BASE + "WHERE id = ?", this::mapItem, id);
    }

    @Override
    public List<ItemDTO> findByCategory(String category) throws SQLException {
        return db.queryList(SELECT_BASE + "WHERE category = ? ORDER BY name ASC", this::mapItem, category);
    }

    @Override
    public ItemDTO create(ItemDTO item) throws SQLException {
        String sql = "INSERT INTO items (name, description, category, price, image_url) VALUES (?, ?, ?, ?, ?)";
        int id = db.insertAndGetId(sql, item.getName(), item.getDescription(), item.getCategory(), item.getPrice(), item.getImageUrl());
        return new ItemDTO(id, item.getName(), item.getDescription(), item.getPrice(), item.getImageUrl(), item.getCategory());
    }

    @Override
    public boolean update(ItemDTO item) throws SQLException {
        String sql = "UPDATE items SET name = ?, description = ?, category = ?, price = ?, image_url = ? WHERE id = ?";
        return db.update(sql, item.getName(), item.getDescription(), item.getCategory(), item.getPrice(), item.getImageUrl(), item.getId()) > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return db.update("DELETE FROM items WHERE id = ?", id) > 0;
    }

    private ItemDTO mapItem(ResultSet rs) throws SQLException {
        return new ItemDTO(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getBigDecimal("price"), rs.getString("image_url"), rs.getString("category"));
    }
}
