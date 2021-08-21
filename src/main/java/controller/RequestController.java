package controller;

import db.Database;
import model.Notification;
import model.Profile;
import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class RequestController
{
    private static final Logger logger = LogManager.getLogger(RequestController.class);

    public void accept(long notificationId)
    {
        try
        {
            Notification notification = Database.getDB().loadNotification(notificationId);

            User pendingUser = Database.getDB().loadUser(notification.getOwner());
            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.addToFollowers(notification.getRequestFrom());
            pendingProfile.removeFromRequests(notification.getRequestFrom());
            requestedProfile.removeFromPending(notification.getOwner());
            requestedProfile.addToFollowings(notification.getOwner());

            Notification accept = new Notification(requestedProfile.getId(), pendingUser.getUsername() + " accepted your follow request.");
            accept = Database.getDB().saveNotification(accept);
            requestedProfile.addToNotifications(accept.getId());

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while accepting notification %s", e, notificationId));
        }
    }

    public void goodReject(long notificationId)
    {
        try
        {
            Notification notification = Database.getDB().loadNotification(notificationId);

            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.removeFromRequests(notification.getRequestFrom());
            requestedProfile.removeFromPending(notification.getOwner());

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while rejecting notification %s", e, notificationId));
        }
    }

    public void badReject(long notificationId)
    {
        try
        {
            Notification notification = Database.getDB().loadNotification(notificationId);

            User pendingUser = Database.getDB().loadUser(notification.getOwner());
            User requestedUser = Database.getDB().loadUser(notification.getRequestFrom());
            Profile pendingProfile = Database.getDB().loadProfile(notification.getOwner());
            Profile requestedProfile = Database.getDB().loadProfile(notification.getRequestFrom());

            pendingProfile.removeFromRequests(requestedUser.getId());
            requestedProfile.removeFromPending(pendingUser.getId());

            Notification roast = new Notification(requestedUser.getId(), pendingUser.getUsername() + " rejected your follow request.");
            roast = Database.getDB().saveNotification(roast);
            requestedProfile.addToNotifications(roast.getId());

            Database.getDB().saveProfile(pendingProfile);
            Database.getDB().saveProfile(requestedProfile);
        }
        catch (SQLException e)
        {
            logger.error(String.format("%s: database error while rejecting notification %s", e, notificationId));
        }
    }
}
