package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.FriendshipDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.FriendshipStatus;
import com.dreamstop.server.dao.FriendshipDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Standard JDBC implementation of {@link FriendshipDAO}.
 */
public class FriendshipDAOImpl implements FriendshipDAO {

    public FriendshipDAOImpl() {
    }

    public FriendshipDAOImpl(DatabaseManager db) {
    }

    private static final String SELECT_FRIENDSHIP =
            "SELECT f.id, f.status, " +
            "       req.id AS req_id, req.username AS req_name, req.email AS req_email, req.balance AS req_bal, req.full_name AS req_full_name, req.avatar_color AS req_color, req.bio AS req_bio, " +
            "       addr.id AS addr_id, addr.username AS addr_name, addr.email AS addr_email, addr.balance AS addr_bal, addr.full_name AS addr_full_name, addr.avatar_color AS addr_color, addr.bio AS addr_bio " +
            "FROM friendships f JOIN users req ON f.requester_id = req.id JOIN users addr ON f.addressee_id = addr.id ";

    @Override
    public List<UserDTO> getFriends(int userId) throws SQLException {
        List<UserDTO> friends = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.email, u.balance, u.full_name, u.avatar_color, u.bio FROM users u WHERE u.id IN (" +
                     "SELECT CASE WHEN f.requester_id = ? THEN f.addressee_id ELSE f.requester_id END " +
                     "FROM friendships f WHERE (f.requester_id = ? OR f.addressee_id = ?) AND f.status = 'ACCEPTED') ORDER BY u.username ASC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    friends.add(new UserDTO(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getBigDecimal("balance"),
                            rs.getString("full_name"),
                            rs.getString("avatar_color"),
                            rs.getString("bio")
                    ));
                }
            }
        }
        return friends;
    }

    @Override
    public List<FriendshipDTO> getIncomingRequests(int userId) throws SQLException {
        List<FriendshipDTO> requests = new ArrayList<>();
        String sql = SELECT_FRIENDSHIP + "WHERE f.addressee_id = ? AND f.status = 'PENDING' ORDER BY f.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapFriendship(rs));
                }
            }
        }
        return requests;
    }

    @Override
    public List<FriendshipDTO> getOutgoingRequests(int userId) throws SQLException {
        List<FriendshipDTO> requests = new ArrayList<>();
        String sql = SELECT_FRIENDSHIP + "WHERE f.requester_id = ? AND f.status = 'PENDING' ORDER BY f.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapFriendship(rs));
                }
            }
        }
        return requests;
    }

    @Override
    public FriendshipDTO sendFriendRequest(int requesterId, int addresseeId) throws SQLException {
        if (requesterId == addresseeId) {
            throw new IllegalArgumentException("Cannot friend oneself");
        }

        Optional<FriendshipStatus> status = getFriendshipStatus(requesterId, addresseeId);
        if (status.isPresent()) {
            if (status.get() == FriendshipStatus.ACCEPTED) throw new SQLException("Already friends");
            if (status.get() == FriendshipStatus.PENDING) throw new SQLException("Request already pending");

            // Re-open previously declined request
            String updateSql = "UPDATE friendships SET requester_id = ?, addressee_id = ?, status = 'PENDING' " +
                               "WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, requesterId);
                stmt.setInt(2, addresseeId);
                stmt.setInt(3, requesterId);
                stmt.setInt(4, addresseeId);
                stmt.setInt(5, addresseeId);
                stmt.setInt(6, requesterId);
                stmt.executeUpdate();
            }
        } else {
            String insertSql = "INSERT INTO friendships (requester_id, addressee_id, status) VALUES (?, ?, 'PENDING')";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, requesterId);
                stmt.setInt(2, addresseeId);
                stmt.executeUpdate();
            }
        }

        String fetchSql = SELECT_FRIENDSHIP + "WHERE (f.requester_id = ? AND f.addressee_id = ?) OR (f.requester_id = ? AND f.addressee_id = ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(fetchSql)) {
            stmt.setInt(1, requesterId);
            stmt.setInt(2, addresseeId);
            stmt.setInt(3, addresseeId);
            stmt.setInt(4, requesterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapFriendship(rs);
                }
            }
        }

        throw new SQLException("Failed to load friend request");
    }

    @Override
    public Optional<FriendshipDTO> getFriendshipById(int requestId) throws SQLException {
        String sql = SELECT_FRIENDSHIP + "WHERE f.id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapFriendship(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean acceptFriendRequest(int requestId) throws SQLException {
        String sql = "UPDATE friendships SET status = 'ACCEPTED' WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean acceptFriendRequest(int requesterId, int addresseeId) throws SQLException {
        String sql = "UPDATE friendships SET status = 'ACCEPTED' WHERE requester_id = ? AND addressee_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requesterId);
            stmt.setInt(2, addresseeId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean declineFriendRequest(int requestId) throws SQLException {
        String sql = "UPDATE friendships SET status = 'DECLINED' WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeFriend(int userId1, int userId2) throws SQLException {
        String sql = "DELETE FROM friendships WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<FriendshipStatus> getFriendshipStatus(int userId1, int userId2) throws SQLException {
        String sql = "SELECT status FROM friendships WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(FriendshipStatus.valueOf(rs.getString("status")));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean areFriends(int userId1, int userId2) throws SQLException {
        return getFriendshipStatus(userId1, userId2).map(s -> s == FriendshipStatus.ACCEPTED).orElse(false);
    }

    private FriendshipDTO mapFriendship(ResultSet rs) throws SQLException {
        UserDTO req = new UserDTO(
                rs.getInt("req_id"),
                rs.getString("req_name"),
                rs.getString("req_email"),
                rs.getBigDecimal("req_bal"),
                rs.getString("req_full_name"),
                rs.getString("req_color"),
                rs.getString("req_bio")
        );
        UserDTO addr = new UserDTO(
                rs.getInt("addr_id"),
                rs.getString("addr_name"),
                rs.getString("addr_email"),
                rs.getBigDecimal("addr_bal"),
                rs.getString("addr_full_name"),
                rs.getString("addr_color"),
                rs.getString("addr_bio")
        );
        return new FriendshipDTO(
                rs.getInt("id"),
                req,
                addr,
                FriendshipStatus.valueOf(rs.getString("status"))
        );
    }
}
