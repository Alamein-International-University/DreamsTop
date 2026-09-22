package com.dreamstop.server.dao;

import com.dreamstop.common.dto.FriendshipDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.FriendshipStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for friendship relationships and friend requests.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public interface FriendshipDAO {

    /**
     * Retrieves all accepted friends for a given user.
     */
    List<UserDTO> getFriends(int userId) throws SQLException;

    /**
     * Retrieves all pending incoming friend requests for a user.
     */
    List<FriendshipDTO> getIncomingRequests(int userId) throws SQLException;

    /**
     * Retrieves all pending outgoing friend requests sent by a user.
     */
    List<FriendshipDTO> getOutgoingRequests(int userId) throws SQLException;

    /**
     * Sends a friend request from requester to addressee.
     */
    FriendshipDTO sendFriendRequest(int requesterId, int addresseeId) throws SQLException;

    /**
     * Finds a friendship record by its request ID.
     */
    Optional<FriendshipDTO> getFriendshipById(int requestId) throws SQLException;

    /**
     * Accepts a pending friend request by request ID.
     */
    boolean acceptFriendRequest(int requestId) throws SQLException;

    /**
     * Accepts a pending friend request by requester and addressee IDs.
     */
    boolean acceptFriendRequest(int requesterId, int addresseeId) throws SQLException;

    /**
     * Declines a pending friend request.
     */
    boolean declineFriendRequest(int requestId) throws SQLException;

    /**
     * Removes an existing friendship between two users.
     */
    boolean removeFriend(int userId1, int userId2) throws SQLException;

    /**
     * Checks friendship status between two users.
     */
    Optional<FriendshipStatus> getFriendshipStatus(int userId1, int userId2) throws SQLException;

    /**
     * Checks if two users are already accepted friends.
     */
    boolean areFriends(int userId1, int userId2) throws SQLException;
}
