package com.dreamstop.server.dao;

import com.dreamstop.common.dto.RegisterRequestDTO;
import com.dreamstop.common.dto.UserDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for user management and authentication.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public interface UserDAO {

    /**
     * Registers a new user with hashed password.
     */
    UserDTO register(RegisterRequestDTO request, String fullName, String avatarColor, String bio) throws SQLException;

    /**
     * Authenticates a user by username or email and plain text password.
     */
    Optional<UserDTO> authenticate(String usernameOrEmail, String plainPassword) throws SQLException;

    /**
     * Finds a user by their primary key ID.
     */
    Optional<UserDTO> findById(int id) throws SQLException;

    /**
     * Finds a user by their username.
     */
    Optional<UserDTO> findByUsername(String username) throws SQLException;

    /**
     * Finds a user by their email address.
     */
    Optional<UserDTO> findByEmail(String email) throws SQLException;

    /**
     * Searches for users by partial match on username, full name, or email,
     * excluding the specified current user ID.
     */
    List<UserDTO> searchUsers(String query, int excludeUserId) throws SQLException;

    /**
     * Recharges a user's wallet balance by the specified amount.
     */
    boolean rechargeBalance(int userId, BigDecimal amount) throws SQLException;

    /**
     * Deducts a specific amount from a user's balance within a given transaction connection.
     */
    boolean deductBalance(int userId, BigDecimal amount, Connection conn) throws SQLException;

    /**
     * Adds an amount to a user's balance within a given transaction connection.
     */
    boolean addBalance(int userId, BigDecimal amount, Connection conn) throws SQLException;

    /**
     * Retrieves the current balance of a user.
     */
    BigDecimal getBalance(int userId) throws SQLException;

    /**
     * Updates a user's profile details (full name, avatar color, bio).
     */
    boolean updateProfile(int userId, String fullName, String avatarColor, String bio) throws SQLException;
}
