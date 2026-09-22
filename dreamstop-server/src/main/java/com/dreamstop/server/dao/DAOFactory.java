package com.dreamstop.server.dao;

import com.dreamstop.server.dao.impl.ContributionDAOImpl;
import com.dreamstop.server.dao.impl.FriendshipDAOImpl;
import com.dreamstop.server.dao.impl.ItemDAOImpl;
import com.dreamstop.server.dao.impl.NotificationDAOImpl;
import com.dreamstop.server.dao.impl.UserDAOImpl;
import com.dreamstop.server.dao.impl.WishlistDAOImpl;

/**
 * Central singleton factory providing access to all Data Access Objects (DAOs).
 * Provides thread-safe, decoupled access to the persistence layer.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public final class DAOFactory {

    private static volatile DAOFactory instance;

    private final UserDAO userDAO;
    private final ItemDAO itemDAO;
    private final WishlistDAO wishlistDAO;
    private final FriendshipDAO friendshipDAO;
    private final NotificationDAO notificationDAO;
    private final ContributionDAO contributionDAO;

    private DAOFactory() {
        this.userDAO = new UserDAOImpl();
        this.itemDAO = new ItemDAOImpl();
        this.wishlistDAO = new WishlistDAOImpl();
        this.friendshipDAO = new FriendshipDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
        this.contributionDAO = new ContributionDAOImpl(this.notificationDAO);
    }

    public static DAOFactory getInstance() {
        if (instance == null) {
            synchronized (DAOFactory.class) {
                if (instance == null) {
                    instance = new DAOFactory();
                }
            }
        }
        return instance;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public ItemDAO getItemDAO() {
        return itemDAO;
    }

    public WishlistDAO getWishlistDAO() {
        return wishlistDAO;
    }

    public FriendshipDAO getFriendshipDAO() {
        return friendshipDAO;
    }

    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }

    public ContributionDAO getContributionDAO() {
        return contributionDAO;
    }
}
