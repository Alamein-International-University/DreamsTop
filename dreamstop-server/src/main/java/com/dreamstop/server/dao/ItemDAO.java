package com.dreamstop.server.dao;

import com.dreamstop.common.dto.ItemDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for store catalog items.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public interface ItemDAO {

    /**
     * Retrieves all available items in the store catalog.
     */
    List<ItemDTO> getAllItems() throws SQLException;

    /**
     * Finds a single catalog item by its ID.
     */
    Optional<ItemDTO> findById(int id) throws SQLException;

    /**
     * Retrieves all items belonging to a specific category.
     */
    List<ItemDTO> findByCategory(String category) throws SQLException;

    /**
     * Inserts a new catalog item (e.g. by admin or database insertion).
     */
    ItemDTO create(ItemDTO item) throws SQLException;

    /**
     * Updates an existing catalog item.
     */
    boolean update(ItemDTO item) throws SQLException;

    /**
     * Deletes an item from the catalog by ID.
     */
    boolean delete(int id) throws SQLException;
}
