package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.FriendshipDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.FriendshipStatus;
import com.dreamstop.server.dao.FriendshipDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concise, clean JDBC implementation of {@link FriendshipDAO}.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class FriendshipDAOImpl implements FriendshipDAO {

    private final DatabaseManager db;

    public FriendshipDAOImpl() {
        this(DatabaseManager.getInstance());
    }

    public FriendshipDAOImpl(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_FRIENDSHIP =
            "SELECT f.id, f.status, " +
            "       req.id AS req_id, req.username AS req_name, req.email AS req_email, req.balance AS req_bal, " +
            "       addr.id AS addr_id, addr.username AS addr_name, addr.email AS addr_email, addr.balance AS addr_bal " +
            "FROM friendships f JOIN users req ON f.requester_id = req.id JOIN users addr ON f.addressee_id = addr.id ";

    @Override
    public List<UserDTO> getFriends(int userId) throws SQLException {
        String sql = "SELECT u.id, u.username, u.email, u.balance FROM users u WHERE u.id IN (" +
                     "SELECT CASE WHEN f.requester_id = ? THEN f.addressee_id ELSE f.requester_id END " +
                     "FROM friendships f WHERE (f.requester_id = ? OR f.addressee_id = ?) AND f.status = 'ACCEPTED') ORDER BY u.username ASC";
        return db.queryList(sql, rs -> new UserDTO(rs.getInt("id"), rs.getString("username"), rs.getString("email"), rs.getBigDecimal("balance")), userId, userId, userId);
    }

    @Override
    public List<FriendshipDTO> getIncomingRequests(int userId) throws SQLException {
        return db.queryList(SELECT_FRIENDSHIP + "WHERE f.addressee_id = ? AND f.status = 'PENDING' ORDER BY f.created_at DESC", this::mapFriendship, userId);
    }

    @Override
    public List<FriendshipDTO> getOutgoingRequests(int userId) throws SQLException {
        return db.queryList(SELECT_FRIENDSHIP + "WHERE f.requester_id = ? AND f.status = 'PENDING' ORDER BY f.created_at DESC", this::mapFriendship, userId);
    }

    @Override
    public FriendshipDTO sendFriendRequest(int requesterId, int addresseeId) throws SQLException {
        if (requesterId == addresseeId) throw new IllegalArgumentException("Cannot friend oneself");

        Optional<FriendshipStatus> status = getFriendshipStatus(requesterId, addresseeId);
        if (status.isPresent()) {
            if (status.get() == FriendshipStatus.ACCEPTED) throw new SQLException("Already friends");
            if (status.get() == FriendshipStatus.PENDING) throw new SQLException("Request already pending");
            // Re-open declined request
            db.update("UPDATE friendships SET requester_id = ?, addressee_id = ?, status = 'PENDING' WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)",
                    requesterId, addresseeId, requesterId, addresseeId, addresseeId, requesterId);
        } else {
            db.update("INSERT INTO friendships (requester_id, addressee_id, status) VALUES (?, ?, 'PENDING')", requesterId, addresseeId);
        }

        return db.queryOne(SELECT_FRIENDSHIP + "WHERE (f.requester_id = ? AND f.addressee_id = ?) OR (f.requester_id = ? AND f.addressee_id = ?)",
                this::mapFriendship, requesterId, addresseeId, addresseeId, requesterId)
                .orElseThrow(() -> new SQLException("Failed to load friend request"));
    }

    @Override
    public Optional<FriendshipDTO> getFriendshipById(int requestId) throws SQLException {
        return db.queryOne(SELECT_FRIENDSHIP + "WHERE f.id = ?", this::mapFriendship, requestId);
    }

    @Override
    public boolean acceptFriendRequest(int requestId) throws SQLException {
        return db.update("UPDATE friendships SET status = 'ACCEPTED' WHERE id = ? AND status = 'PENDING'", requestId) > 0;
    }

    @Override
    public boolean acceptFriendRequest(int requesterId, int addresseeId) throws SQLException {
        return db.update("UPDATE friendships SET status = 'ACCEPTED' WHERE requester_id = ? AND addressee_id = ? AND status = 'PENDING'", requesterId, addresseeId) > 0;
    }

    @Override
    public boolean declineFriendRequest(int requestId) throws SQLException {
        return db.update("UPDATE friendships SET status = 'DECLINED' WHERE id = ? AND status = 'PENDING'", requestId) > 0;
    }

    @Override
    public boolean removeFriend(int userId1, int userId2) throws SQLException {
        return db.update("DELETE FROM friendships WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)",
                userId1, userId2, userId2, userId1) > 0;
    }

    @Override
    public Optional<FriendshipStatus> getFriendshipStatus(int userId1, int userId2) throws SQLException {
        return db.queryOne("SELECT status FROM friendships WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)",
                rs -> FriendshipStatus.valueOf(rs.getString("status")), userId1, userId2, userId2, userId1);
    }

    @Override
    public boolean areFriends(int userId1, int userId2) throws SQLException {
        return getFriendshipStatus(userId1, userId2).map(s -> s == FriendshipStatus.ACCEPTED).orElse(false);
    }

    private FriendshipDTO mapFriendship(ResultSet rs) throws SQLException {
        UserDTO req = new UserDTO(rs.getInt("req_id"), rs.getString("req_name"), rs.getString("req_email"), rs.getBigDecimal("req_bal"));
        UserDTO addr = new UserDTO(rs.getInt("addr_id"), rs.getString("addr_name"), rs.getString("addr_email"), rs.getBigDecimal("addr_bal"));
        return new FriendshipDTO(rs.getInt("id"), req, addr, FriendshipStatus.valueOf(rs.getString("status")));
    }
}
